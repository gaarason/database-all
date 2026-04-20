<#
.SYNOPSIS
运行仓库全量单元测试并落盘日志。

.DESCRIPTION
脚本在仓库根目录执行 Maven 测试：
- 默认匹配 *Tests
- 支持排除指定测试类
- 强制 failIfNoTests=false
- 日志写入 tmp/mvn-test-all-时间戳.log

.PARAMETER TestPattern
Surefire 测试匹配模式，默认 *Tests。

.PARAMETER ExcludeTests
要排除的测试类名数组，例如 AaaGcTests、DruidApplicationTests。

.EXAMPLE
./scripts/test-all.ps1

.EXAMPLE
./scripts/test-all.ps1 -ExcludeTests @("AaaGcTests", "DruidApplicationTests")
#>
param(
    [string]$TestPattern = "*Tests",
    [string[]]$ExcludeTests = @()
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$tmpDir = Join-Path $repoRoot "tmp"
if (-not (Test-Path $tmpDir)) {
    New-Item -Path $tmpDir -ItemType Directory | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$logPath = Join-Path $tmpDir "mvn-test-all-$timestamp.log"

$resolvedPattern = $TestPattern
if ($ExcludeTests.Count -gt 0) {
    foreach ($name in $ExcludeTests) {
        if (-not [string]::IsNullOrWhiteSpace($name)) {
            $resolvedPattern += ",!$name"
        }
    }
}

$mvnArgs = @(
    "test",
    "-Dtest=$resolvedPattern",
    "-DfailIfNoTests=false"
)

Write-Host "[test-all] repo   : $repoRoot"
Write-Host "[test-all] pattern: $resolvedPattern"
Write-Host "[test-all] log    : $logPath"

& mvn @mvnArgs 2>&1 | Tee-Object -FilePath $logPath
$exitCode = $LASTEXITCODE

Write-Host "[test-all] exitCode: $exitCode"
if ($exitCode -ne 0) {
    Write-Host "[test-all] FAIL. See log: $logPath"
    exit $exitCode
}

Write-Host "[test-all] SUCCESS. See log: $logPath"
