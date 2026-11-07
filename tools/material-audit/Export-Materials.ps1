param(
    [Parameter(Mandatory)][string]$PaperJar,
    [Parameter(Mandatory)][string]$Java,
    [string]$Output = '',
    [string]$Baseline = '',
    [switch]$ValidateLibrary
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$PaperJar = (Resolve-Path -LiteralPath $PaperJar).Path
$Java = (Resolve-Path -LiteralPath $Java).Path
$runtimeRoot = Join-Path $projectRoot '.runtime/material-audit'
[IO.Directory]::CreateDirectory($runtimeRoot) | Out-Null
if (!$Output) { $Output = Join-Path $runtimeRoot 'materials-current.csv' }
$Output = [IO.Path]::GetFullPath($Output)
Push-Location $runtimeRoot
try {
    & $Java '-Dpaperclip.patchonly=true' -jar $PaperJar
    if ($LASTEXITCODE -ne 0) { throw 'Paperclip dependency extraction failed' }
    $archive = [IO.Compression.ZipFile]::OpenRead($PaperJar)
    try {
        function Read-ZipText([string]$Name) {
            $reader = [IO.StreamReader]::new($archive.GetEntry($Name).Open())
            try { $reader.ReadToEnd() } finally { $reader.Dispose() }
        }
        $jars = @()
        foreach ($line in ((Read-ZipText 'META-INF/versions.list') -split "`n")) {
            if ($line.Trim()) { $jars += (Join-Path $runtimeRoot ('versions/' + ($line.Trim() -split "`t")[2])) }
        }
        foreach ($line in ((Read-ZipText 'META-INF/libraries.list') -split "`n")) {
            if ($line.Trim()) { $jars += (Join-Path $runtimeRoot ('libraries/' + ($line.Trim() -split "`t")[2])) }
        }
    } finally { $archive.Dispose() }
    [IO.File]::WriteAllText((Join-Path $runtimeRoot 'classpath.txt'), ($jars -join [IO.Path]::PathSeparator))
    $exportArgs = @($Output)
    if ($ValidateLibrary) {
        $exportArgs += (Join-Path $projectRoot 'src/main/resources/block-pools/material-metadata.tsv')
        $exportArgs += (Join-Path $projectRoot 'src/main/resources/block-pools')
    }
    & $Java '--enable-native-access=ALL-UNNAMED' '--class-path' ($jars -join [IO.Path]::PathSeparator) (Join-Path $PSScriptRoot 'ExportMaterials.java') @exportArgs
    if ($LASTEXITCODE -ne 0) { throw 'Bukkit catalogue export failed' }
} finally { Pop-Location }
if ($Baseline) {
    $old = @(Import-Csv -LiteralPath $Baseline)
    $current = @(Import-Csv -LiteralPath $Output)
    Compare-Object $old $current -Property id, is_block, is_item, is_air, has_block_item, translation_key, enabled_by_default | Format-Table -AutoSize
}
Write-Host "Exported actual Bukkit Material registry to $Output"
