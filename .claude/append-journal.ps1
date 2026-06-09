# Appends one row to ai-jourinal.md per completed turn (Stop hook).
# Reads the Stop hook's stdin JSON, opens the transcript it points to, and pulls
# out the last genuine user prompt + the last assistant text response.
# "When" / "What was asked" / "What the response was" are filled automatically;
# "Accepted / Rejected" stays _Pending_ for you to fill in by hand.

$raw = [Console]::In.ReadToEnd()

# --- TEMP DEBUG: proves the hook fired at all, even if logic below bails out ---
try {
  $dbg = Join-Path $PSScriptRoot '.journal-debug.log'
  Add-Content -LiteralPath $dbg -Value ("[" + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss') + "] fired; rawlen=" + $raw.Length + "; raw=" + ($raw -replace '[\r\n]+',' ')) -Encoding utf8
} catch {}
# --- end TEMP DEBUG ---

try { $j = $raw | ConvertFrom-Json } catch { exit 0 }

$tp = "$($j.transcript_path)"
if ([string]::IsNullOrWhiteSpace($tp) -or -not (Test-Path -LiteralPath $tp)) { exit 0 }

$lines = Get-Content -LiteralPath $tp
if (-not $lines) { exit 0 }

function Get-Text($c) {
  if ($null -eq $c) { return '' }
  if ($c -is [string]) { return $c }
  return (($c | Where-Object { $_.type -eq 'text' } | ForEach-Object { $_.text }) -join ' ')
}

function Clean($s, $max) {
  $s = ($s -replace '[\r\n]+', ' ').Trim()
  $s = $s -replace '\|', '/'
  if ($s.Length -gt $max) { $s = $s.Substring(0, $max - 3) + '...' }
  return $s
}

# Last genuine user prompt (has text, not an injected/meta message).
$userTxt = ''; $userTs = $null
for ($i = $lines.Count - 1; $i -ge 0; $i--) {
  $o = $null; try { $o = $lines[$i] | ConvertFrom-Json } catch { continue }
  if ($o.type -ne 'user') { continue }
  if ($o.isMeta -eq $true) { continue }
  $t = Get-Text $o.message.content
  if (-not [string]::IsNullOrWhiteSpace($t)) { $userTxt = $t; $userTs = $o.timestamp; break }
}
if ([string]::IsNullOrWhiteSpace($userTxt)) { exit 0 }

# Last assistant text response + its uuid (used for dedup).
$asstTxt = ''; $asstUuid = ''
for ($i = $lines.Count - 1; $i -ge 0; $i--) {
  $o = $null; try { $o = $lines[$i] | ConvertFrom-Json } catch { continue }
  if ($o.type -ne 'assistant') { continue }
  $t = Get-Text $o.message.content
  if (-not [string]::IsNullOrWhiteSpace($t)) { $asstTxt = $t; $asstUuid = $o.uuid; break }
}

# Dedup: skip if we already logged this assistant message (Stop can re-fire on
# resume/clear/compact without new content).
$stateFile = Join-Path $PSScriptRoot '.journal-last'
if ($asstUuid -and (Test-Path -LiteralPath $stateFile)) {
  if ((Get-Content -LiteralPath $stateFile -Raw).Trim() -eq $asstUuid) { exit 0 }
}

$userTxt = Clean $userTxt 200
$asstTxt = if ([string]::IsNullOrWhiteSpace($asstTxt)) { '_(no text response)_' } else { Clean $asstTxt 300 }

$when = Get-Date -Format 'yyyy-MM-dd HH:mm'
if ($userTs) { try { $when = ([datetimeoffset]$userTs).LocalDateTime.ToString('yyyy-MM-dd HH:mm') } catch {} }

$row = "| $when | $userTxt | $asstTxt | _Pending_ |"
$journal = Join-Path (Split-Path $PSScriptRoot -Parent) 'ai-jourinal.md'

# Guarantee the row starts on its own line even if the file lacks a trailing newline.
$existing = ''
if (Test-Path -LiteralPath $journal) { $existing = [System.IO.File]::ReadAllText($journal) }
if ($existing.Length -gt 0 -and -not ($existing.EndsWith("`n"))) { $row = "`r`n" + $row }
Add-Content -LiteralPath $journal -Value $row -Encoding utf8

if ($asstUuid) { Set-Content -LiteralPath $stateFile -Value $asstUuid -Encoding ascii }
exit 0
