param([Parameter(Mandatory)][string]$Java)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$Java = (Resolve-Path -LiteralPath $Java).Path
$runtimeRoot = Join-Path $projectRoot '.runtime/material-audit'
$classes = Join-Path $projectRoot 'target/classes'
if (!(Test-Path -LiteralPath $classes)) { throw 'Run mvn test first' }
$classpath = $classes + [IO.Path]::PathSeparator + (Get-Content -LiteralPath (Join-Path $runtimeRoot 'classpath.txt') -Raw)
Push-Location $runtimeRoot
try {
    & $Java '--enable-native-access=ALL-UNNAMED' '--class-path' $classpath (Join-Path $PSScriptRoot 'VerifyTaskRuntime.java') $projectRoot
    if ($LASTEXITCODE -ne 0) { throw 'Real-registry task verification failed' }
    Get-Content -LiteralPath (Join-Path $runtimeRoot 'task-runtime-verification.json')
} finally { Pop-Location }
