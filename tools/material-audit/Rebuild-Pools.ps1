param(
    [string]$ApiSnapshot = '',
    [switch]$Check
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
if (!$ApiSnapshot) { $ApiSnapshot = Join-Path $projectRoot 'docs/block-library/materials-api-26.3.csv' }
$poolRoot = Join-Path $projectRoot 'src/main/resources/block-pools'
$api = @(Import-Csv -LiteralPath $ApiSnapshot)
$chinese = Get-Content -LiteralPath (Join-Path $projectRoot 'src/main/resources/zh_cn.json') -Raw | ConvertFrom-Json -AsHashtable
$apiById = @{}
foreach ($material in $api) { $apiById[$material.id] = $material }
$dimensionNames = @{ OVERWORLD = '主世界'; NETHER = '下界'; END = '末地' }
$tierNames = @{ 1 = '基础'; 2 = '常见'; 3 = '探索'; 4 = '进阶'; 5 = '稀有'; 6 = '极限' }
function Get-ChineseName([string]$Id) {
    $key = $apiById[$Id].translation_key
    if (!$key -or !$chinese.ContainsKey($key)) { throw "Missing zh_cn translation for $Id ($key)" }
    return $chinese[$key]
}
function Escape-MarkdownCell([string]$Text) { return $Text.Replace('|', '\|').Replace("`r", ' ').Replace("`n", ' ') }
function Get-ChineseReason([string]$Text) {
    foreach ($wood in @('oak', 'birch', 'spruce', 'acacia', 'dark_oak', 'jungle', 'cherry', 'mangrove', 'pale_oak', 'poplar')) {
        $source = "采集${wood}树木或其树苗"
        if ($Text.Contains($source)) {
            $saplingId = if ($wood -eq 'mangrove') { 'minecraft:mangrove_propagule' } else { "minecraft:${wood}_sapling" }
            $Text = $Text.Replace($source, ('采集' + (Get-ChineseName "minecraft:${wood}_log") + '或' + (Get-ChineseName $saplingId)))
        }
    }
    return $Text
}
$metadata = @(Import-Csv -LiteralPath (Join-Path $poolRoot 'material-metadata.tsv') -Delimiter "`t")
$byId = @{}
foreach ($row in $metadata) {
    if ($byId.ContainsKey($row.id)) { throw "Duplicate metadata: $($row.id)" }
    if ($row.id -cnotmatch '^minecraft:[a-z0-9_]+$') { throw "Noncanonical ID: $($row.id)" }
    if (!$row.reason -or !$row.evidence) { throw "Missing review rationale/evidence: $($row.id)" }
    if ($row.survival_obtainable -notin @('true', 'false')) { throw "Invalid survival flag: $($row.id)" }
    if ($row.status -eq 'included') {
        if ($row.dimension -notin @('OVERWORLD', 'NETHER', 'END') -or $row.tier -notmatch '^[1-6]$' -or $row.survival_obtainable -ne 'true') {
            throw "Invalid classification: $($row.id)"
        }
    } elseif ($row.status -ne 'excluded' -or $row.dimension -or $row.tier) {
        throw "Invalid exclusion: $($row.id)"
    }
    $byId[$row.id] = $row
}
$candidateIds = @{}
foreach ($material in $api) {
    if ($material.is_block -ne 'true' -or $material.is_item -ne 'true' -or $material.is_air -eq 'true') { continue }
    $candidateIds[$material.id] = $true
    if (!$byId.ContainsKey($material.id)) { throw "Unreviewed API candidate: $($material.id). Review it before adding to any pool." }
    if ($byId[$material.id].status -eq 'included' -and $material.has_block_item -ne 'true') { throw "Not a block item: $($material.id)" }
    if ($byId[$material.id].status -eq 'included' -and $material.enabled_by_default -ne 'true') { throw "Experimental item: $($material.id)" }
}
foreach ($id in $byId.Keys) {
    if (!$candidateIds.ContainsKey($id)) { throw "Removed/renamed/noncandidate Material: $id" }
}
$categoryNames = @{ GATHER = '常规采集'; CRAFT = '加工制作'; EXPLORE = '探索获取' }
$acquisition = @{}
foreach ($row in (Import-Csv -LiteralPath (Join-Path $poolRoot 'acquisition-tags.tsv') -Delimiter "`t")) {
    if ($acquisition.ContainsKey($row.id) -or !$categoryNames.ContainsKey($row.category) -or !$row.reason -or !$byId.ContainsKey($row.id) -or $byId[$row.id].status -ne 'included') {
        throw "Invalid/duplicate acquisition tag: $($row.id)"
    }
    $acquisition[$row.id] = $row
}
foreach ($row in ($metadata | Where-Object status -eq 'included')) {
    if (!$acquisition.ContainsKey($row.id)) { throw "Missing reviewed acquisition tag: $($row.id)" }
}
$utf8 = [Text.UTF8Encoding]::new($false)
function Write-OutputFile([string]$Path, [string]$Content) {
    $Content = $Content.Replace("`r`n", "`n")
    if ($Check) {
        if (!(Test-Path -LiteralPath $Path) -or [IO.File]::ReadAllText($Path).Replace("`r`n", "`n") -cne $Content) { throw "Stale generated file: $Path" }
    } else {
        [IO.Directory]::CreateDirectory([IO.Path]::GetDirectoryName($Path)) | Out-Null
        [IO.File]::WriteAllText($Path, $Content, $utf8)
    }
}
$stats = @()
$fullList = [Collections.Generic.List[string]]::new()
$fullList.Add('# 方块库完整分类清单（Paper 26.3 build 244）')
$fullList.Add('')
$fullList.Add('由已审核 material-metadata.tsv 与 acquisition-tags.tsv 生成。每个 ID 仅属于一个维度、等级和主要获取标签；标签用于宏观抽取节奏，不代表简单或快速。')
$fullList.Add('中文名称使用 API 导出的 translation_key 查询项目 zh_cn.json；待复核表示初版平衡判断需要确认。铜附属条目只有开启铜附属方块开关后，才按所在等级加入难度。')
foreach ($dimension in @('OVERWORLD', 'NETHER', 'END')) {
    foreach ($tier in 1..6) {
        $ids = @($metadata | Where-Object { $_.status -eq 'included' -and $_.dimension -eq $dimension -and $_.tier -eq "$tier" } | Sort-Object id | ForEach-Object id)
        $relative = "$($dimension.ToLowerInvariant())/tier-$tier.txt"
        Write-OutputFile (Join-Path $poolRoot $relative) (($ids -join "`n") + $(if ($ids.Count) { "`n" } else { '' }))
        $stats += [pscustomobject]@{ dimension = $dimension; tier = $tier; count = $ids.Count }
        $fullList.Add('')
        $fullList.Add("## $($dimensionNames[$dimension]) / T$tier $($tierNames[$tier])（$($ids.Count) 项）")
        $fullList.Add('')
        $fullList.Add('| 中文名称 | 标准 ID | 主要获取标签 | 获取方式／限制 | 平衡复核 | 铜分组 |')
        $fullList.Add('|---|---|---|---|---|---|')
        foreach ($id in $ids) {
            $review = $byId[$id]
            $needsReview = if ($review.flags -match 'balance_review') { '待复核' } else { '—' }
            $copperGroup = if ($review.flags -match 'copper_extra') { '附属（开关）' } elseif ($review.flags -match 'copper_basic') { '基础（保留）' } else { '—' }
            $category = $categoryNames[$acquisition[$id].category]
            $fullList.Add('| ' + (Escape-MarkdownCell (Get-ChineseName $id)) + ' | `' + $id + '` | ' + $category + ' | ' + (Escape-MarkdownCell (Get-ChineseReason $review.reason)) + " | $needsReview | $copperGroup |")
        }
    }
}
$audit = foreach ($material in ($api | Where-Object is_block -eq 'true' | Sort-Object id)) {
    $review = $byId[$material.id]
    [pscustomobject][ordered]@{
        id = $material.id; name_zh_cn = Get-ChineseName $material.id; translation_key = $material.translation_key
        material = $material.material; is_block = $material.is_block
        is_item = $material.is_item; has_block_item = $material.has_block_item; enabled_by_default = $material.enabled_by_default
        status = $(if ($review) { $review.status } else { 'excluded' })
        survival_obtainable = $(if ($review) { $review.survival_obtainable } else { 'false' })
        dimension = $(if ($review) { $review.dimension } else { '' })
        tier = $(if ($review) { $review.tier } else { '' })
        acquisition = $(if ($acquisition.ContainsKey($material.id)) { $acquisition[$material.id].category } else { '' })
        acquisition_zh_cn = $(if ($acquisition.ContainsKey($material.id)) { $categoryNames[$acquisition[$material.id].category] } else { '' })
        reason = $(if ($review) { Get-ChineseReason $review.reason } elseif ($material.is_air -eq 'true') { '空气不是收集目标' } else { '仅为世界中的方块状态；没有对应的可持有 Material 物品' })
        flags = $(if ($review) { $review.flags } else { '' })
        evidence = $(if ($review) { $review.evidence } else { 'Bukkit Material.isItem() / Material.isAir()' })
    }
}
$reportRoot = Join-Path $projectRoot 'docs/block-library'
$categoryList = [Collections.Generic.List[string]]::new()
$categoryList.Add('# 主要获取标签完整清单')
$categoryList.Add('')
$categoryList.Add('按主要获取成本人工审核，每个目标只有一个标签。加工制作包含合成、熔炼、染色及生产机制，不等于简单；探索获取包括依赖特定群系、结构和专属资源的加工物。维度、T 级与铜开关仍独立生效。')
$categoryStats = @()
foreach ($category in @('GATHER','CRAFT','EXPLORE')) {
    $rows = @($metadata | Where-Object { $_.status -eq 'included' -and $acquisition[$_.id].category -eq $category } | Sort-Object dimension, tier, id)
    $categoryList.Add('')
    $categoryList.Add("## $($categoryNames[$category])（$($rows.Count) 项）")
    $categoryList.Add('')
    $categoryList.Add('| 中文名称 | 标准 ID | 原分类 | 标签依据 |')
    $categoryList.Add('|---|---|---|---|')
    foreach ($row in $rows) {
        $categoryList.Add('| ' + (Get-ChineseName $row.id) + ' | `' + $row.id + '` | ' + $dimensionNames[$row.dimension] + ' T' + $row.tier + ' | ' + (Escape-MarkdownCell $acquisition[$row.id].reason) + ' |')
    }
    foreach ($dimension in @('OVERWORLD','NETHER','END')) {
        foreach ($tier in 1..6) {
            $count = @($rows | Where-Object { $_.dimension -eq $dimension -and $_.tier -eq "$tier" }).Count
            $categoryStats += [pscustomobject][ordered]@{ category = $category; name_zh_cn = $categoryNames[$category]; dimension = $dimension; tier = $tier; count = $count }
        }
    }
}
Write-OutputFile (Join-Path $reportRoot 'acquisition-complete.md') (($categoryList -join "`n") + "`n")
Write-OutputFile (Join-Path $reportRoot 'classification-complete.md') (($fullList -join "`n") + "`n")
$excludedList = [Collections.Generic.List[string]]::new()
$excludedList.Add('# 方块库排除清单（中文核对版）')
$excludedList.Add('')
$excludedList.Add('含不可持有的世界方块状态、正常生存不可获取项、小麦同名映射与龙蛋公平性排除。中文名通过 API 翻译 key 查询 zh_cn.json。')
$excludedList.Add('')
$excludedList.Add('| 中文名称 | 标准 ID | 排除原因 |')
$excludedList.Add('|---|---|---|')
foreach ($row in ($audit | Where-Object status -eq 'excluded')) {
    $excludedList.Add('| ' + (Escape-MarkdownCell $row.name_zh_cn) + ' | `' + $row.id + '` | ' + (Escape-MarkdownCell $row.reason) + ' |')
}
Write-OutputFile (Join-Path $reportRoot 'excluded-blocks.md') (($excludedList -join "`n") + "`n")
$copperList = [Collections.Generic.List[string]]::new()
$copperList.Add('# 铜方块开关范围（中文核对版）')
$copperList.Add('')
$copperList.Add('默认关闭铜附属方块。基础铜制品仍须符合所选难度的维度和等级范围；开启开关只恢复附属条目的原有资格，不提前解锁维度，也不扩大等级范围。')
foreach ($group in @('copper_basic', 'copper_extra')) {
    $rows = @($metadata | Where-Object { $_.status -eq 'included' -and ($_.flags -split ';') -contains $group } | Sort-Object id)
    $groupFile = if ($group -eq 'copper_basic') { 'copper-basic.txt' } else { 'copper-extras.txt' }
    Write-OutputFile (Join-Path $poolRoot $groupFile) ((@($rows | ForEach-Object id) -join "`n") + "`n")
    $label = if ($group -eq 'copper_basic') { '基础铜制品（保留）' } else { '铜附属方块（受开关控制）' }
    $copperList.Add('')
    $copperList.Add("## $label — $($rows.Count) 项")
    $copperList.Add('')
    $copperList.Add('| 中文名称 | 标准 ID | 原分类 |')
    $copperList.Add('|---|---|---|')
    foreach ($row in $rows) {
        $copperList.Add('| ' + (Get-ChineseName $row.id) + ' | `' + $row.id + '` | ' + $dimensionNames[$row.dimension] + ' T' + $row.tier + ' |')
    }
}
Write-OutputFile (Join-Path $reportRoot 'copper-options.md') (($copperList -join "`n") + "`n")
# Standard ranges are fixed. These counts describe bundled defaults, not a server's edited weights.
$presetSpecs = @(
    @{ id = 'casual'; name = '休闲'; caps = @(2,0,0); gates = @('—','—') },
    @{ id = 'easy'; name = '简单'; caps = @(2,2,0); gates = @('50%','—') },
    @{ id = 'normal'; name = '标准'; caps = @(4,3,0); gates = @('25%','—') },
    @{ id = 'hard'; name = '困难'; caps = @(5,5,5); gates = @('25%','70%') },
    @{ id = 'nightmare'; name = '噩梦'; caps = @(6,6,6); gates = @('20%','65%') }
)
$presetCounts = foreach ($spec in $presetSpecs) {
    $eligible = @($metadata | Where-Object { $_.status -eq 'included' -and [int]$_.tier -le $spec.caps[([array]@('OVERWORLD','NETHER','END')).IndexOf($_.dimension)] })
    [pscustomobject][ordered]@{ preset = $spec.id; name_zh_cn = $spec.name
        copper_off = @($eligible | Where-Object { ($_.flags -split ';') -notcontains 'copper_extra' }).Count
        copper_on = $eligible.Count; nether_unlock = $spec.gates[0]; end_unlock = $spec.gates[1] }
}
foreach ($report in @(
    @{ Name = 'counts.csv'; Rows = $stats },
    @{ Name = 'block-audit.csv'; Rows = @($audit) },
    @{ Name = 'excluded-blocks.csv'; Rows = @($audit | Where-Object status -eq 'excluded') },
    @{ Name = 'balance-review.csv'; Rows = @($audit | Where-Object { $_.flags -match 'balance_review' }) },
    @{ Name = 'preset-summary.csv'; Rows = @($presetCounts) },
    @{ Name = 'acquisition-counts.csv'; Rows = @($categoryStats) }
)) {
    Write-OutputFile (Join-Path $reportRoot $report.Name) ((@($report.Rows | ConvertTo-Csv -NoTypeInformation) -join "`n") + "`n")
}
$stats | Format-Table -AutoSize
Write-Host "Reviewed candidates: $($metadata.Count); included: $(@($metadata | Where-Object status -eq 'included').Count); unreviewed: 0."
