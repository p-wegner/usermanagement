# Script to remove all traces of files from .coding-aider-docs/images folder from git history
# Requires git-filter-repo: https://github.com/newren/git-filter-repo

# Exit immediately if a command exits with a non-zero status
$ErrorActionPreference = "Stop"

# Check if git-filter-repo is installed and accessible
$gitFilterRepo = Get-Command git-filter-repo -ErrorAction SilentlyContinue

if (-not $gitFilterRepo) {
    # Try to check if the Python module is installed
    $pythonCheck = python -c "import importlib.util; print(importlib.util.find_spec('git_filter_repo') is not None)" 2>$null
    
    if ($pythonCheck -eq "True") {
        Write-Host "git-filter-repo Python module is installed but not in your PATH."
        Write-Host "Trying to locate the executable..."
        
        # Try to find the executable in Python's scripts directory
        $pythonScriptsPath = python -c "import sys; import os; print(os.path.join(sys.prefix, 'Scripts'))" 2>$null
        $possiblePath = Join-Path -Path $pythonScriptsPath -ChildPath "git-filter-repo"
        $possibleBatPath = Join-Path -Path $pythonScriptsPath -ChildPath "git-filter-repo.bat"
        
        # Also try to find it in site-packages
        $sitePackagesPath = python -c "import site; print(site.getsitepackages()[0])" 2>$null
        $sitePackagesExePath = Join-Path -Path $sitePackagesPath -ChildPath "git_filter_repo.py"
        
        if (Test-Path $possiblePath) {
            Write-Host "Found git-filter-repo at: $possiblePath"
            Write-Host "To add this directory to your PATH permanently, run the following in an Administrator PowerShell:"
            Write-Host "[Environment]::SetEnvironmentVariable('Path', [Environment]::GetEnvironmentVariable('Path', 'User') + ';$pythonScriptsPath', 'User')"
            Write-Host ""
            Write-Host "For this session only, you can run:"
            Write-Host "`$env:Path += ';$pythonScriptsPath'"
            Write-Host ""
            Write-Host "Or use the full path to run the command:"
            Write-Host "& '$possiblePath' --path .coding-aider-docs/images/ --invert-paths --force"
        } elseif (Test-Path $possibleBatPath) {
            Write-Host "Found git-filter-repo.bat at: $possibleBatPath"
            Write-Host "To add this directory to your PATH permanently, run the following in an Administrator PowerShell:"
            Write-Host "[Environment]::SetEnvironmentVariable('Path', [Environment]::GetEnvironmentVariable('Path', 'User') + ';$pythonScriptsPath', 'User')"
            Write-Host ""
            Write-Host "For this session only, you can run:"
            Write-Host "`$env:Path += ';$pythonScriptsPath'"
            Write-Host ""
            Write-Host "Or use the full path to run the command:"
            Write-Host "& '$possibleBatPath' --path .coding-aider-docs/images/ --invert-paths --force"
        } elseif (Test-Path $sitePackagesExePath) {
            Write-Host "Found git_filter_repo.py at: $sitePackagesExePath"
            Write-Host "You can run it directly with Python:"
            Write-Host "python '$sitePackagesExePath' --path .coding-aider-docs/images/ --invert-paths --force"
            Write-Host ""
            Write-Host "To create a proper executable, you can create a batch file in your PATH:"
            Write-Host "1. Create a file named 'git-filter-repo.bat' in a directory in your PATH with the following content:"
            Write-Host "@echo off"
            Write-Host "python ""$sitePackagesExePath"" %*"
            
            # Offer to create the batch file automatically
            Write-Host ""
            Write-Host "Would you like to create this batch file automatically? (y/n)"
            $createBat = Read-Host
            if ($createBat -eq "y" -or $createBat -eq "Y") {
                try {
                    $batContent = "@echo off`npython `"$sitePackagesExePath`" %*"
                    $batPath = Join-Path -Path $pythonScriptsPath -ChildPath "git-filter-repo.bat"
                    Set-Content -Path $batPath -Value $batContent -Force
                    Write-Host "Created batch file at: $batPath"
                    Write-Host "You can now run git-filter-repo if $pythonScriptsPath is in your PATH."
                    
                    # Add to PATH for current session
                    $env:Path += ";$pythonScriptsPath"
                    Write-Host "Added $pythonScriptsPath to PATH for current session."
                    
                    # Try to find git-filter-repo again
                    $gitFilterRepo = Get-Command git-filter-repo -ErrorAction SilentlyContinue
                    if ($gitFilterRepo) {
                        Write-Host "git-filter-repo is now accessible in this session!"
                        # Continue with the script
                        break
                    }
                } catch {
                    Write-Host "Failed to create batch file: $_"
                    Write-Host "You may need to run this script as administrator or create the batch file manually."
                }
            }
        } else {
            Write-Host "Could not locate git-filter-repo executable or script."
            Write-Host "It seems the pip installation failed to create the executable properly."
            Write-Host ""
            Write-Host "Common fix for 'Failed to write executable' error:"
            Write-Host "1. Try installing with the --user flag: pip install --user git-filter-repo"
            Write-Host "2. Or create a manual wrapper as described below."
            Write-Host ""
            
            # Check if the module is definitely installed despite the executable error
            $moduleInstalled = python -c "import importlib.util; print(importlib.util.find_spec('git_filter_repo') is not None)" 2>$null
            if ($moduleInstalled -eq "True") {
                Write-Host "The git-filter-repo module is installed, but the executable is missing."
                Write-Host "Creating a manual wrapper batch file can fix this issue."
                
                # Try to find the module path
                $modulePath = python -c "import importlib.util; import os; spec = importlib.util.find_spec('git_filter_repo'); print(spec.origin if spec else '')" 2>$null
                
                if ($modulePath) {
                    Write-Host "Found module at: $modulePath"
                    Write-Host ""
                    Write-Host "Would you like to create a batch file wrapper automatically? (y/n)"
                    $createBat = Read-Host
                    if ($createBat -eq "y" -or $createBat -eq "Y") {
                        try {
                            $batContent = "@echo off`npython `"$modulePath`" %*"
                            $batPath = Join-Path -Path $pythonScriptsPath -ChildPath "git-filter-repo.bat"
                            Set-Content -Path $batPath -Value $batContent -Force
                            Write-Host "Created batch file at: $batPath"
                            
                            # Add to PATH for current session
                            $env:Path += ";$pythonScriptsPath"
                            Write-Host "Added $pythonScriptsPath to PATH for current session."
                            
                            # Try to find git-filter-repo again
                            $gitFilterRepo = Get-Command git-filter-repo -ErrorAction SilentlyContinue
                            if ($gitFilterRepo) {
                                Write-Host "git-filter-repo is now accessible in this session!"
                                # Continue with the script
                                break
                            }
                        } catch {
                            Write-Host "Failed to create batch file: $_"
                            Write-Host "You may need to run this script as administrator or create the batch file manually."
                        }
                    }
                }
            } else {
                Write-Host "Try installing it again with: pip install git-filter-repo"
                Write-Host "Or make sure the Scripts directory is in your PATH."
            }
        }
    } else {
        Write-Error "Error: git-filter-repo is not installed.`nPlease install it with: pip install git-filter-repo"
    }
    exit 1
}

# Check if we're in a git repository
$isGitRepo = git rev-parse --is-inside-work-tree 2>$null
if (-not $isGitRepo) {
    Write-Error "Error: Not in a git repository."
    exit 1
}

# Check for uncommitted changes
$hasChanges = git diff-index --quiet HEAD -- 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Error "Error: You have uncommitted changes. Please commit or stash them first."
    exit 1
}

# Create a backup branch
$currentBranch = git branch --show-current
$backupBranch = "backup-before-cleanup-$(Get-Date -Format 'yyyyMMddHHmmss')"
Write-Host "Creating backup branch: $backupBranch"
git branch "$backupBranch"

Write-Host "Removing all traces of files from .coding-aider-docs/images folder..."
git filter-repo --path .coding-aider-docs/images/ --invert-paths --force

Write-Host "Done! All traces of files from .coding-aider-docs/images have been removed from the git history."
Write-Host "A backup branch '$backupBranch' has been created with the original history."
Write-Host ""
Write-Host "IMPORTANT: This operation has rewritten git history. If this repository was previously pushed"
Write-Host "to a remote, you will need to force push with: git push --force"
Write-Host ""
Write-Host "To verify the cleanup, you can run: git log --all --name-only --pretty=format: | Select-String -Pattern '.coding-aider-docs/images'"
Write-Host "This should return no results if the cleanup was successful."
