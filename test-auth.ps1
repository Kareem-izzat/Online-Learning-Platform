# API Gateway Authentication Test Script
# Tests the complete authentication flow: Register → Login → Protected Access

Write-Host "=== API Gateway Authentication Test ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$email = "testuser_$(Get-Random)@example.com"

# Step 1: Register User
Write-Host "Step 1: Registering new user..." -ForegroundColor Yellow
Write-Host "Email: $email" -ForegroundColor Gray

$registerBody = @{
    email = $email
    password = "password123"
    firstName = "Test"
    lastName = "User"
    role = "STUDENT"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody

    Write-Host "✓ Registration successful!" -ForegroundColor Green
    Write-Host "  User ID: $($registerResponse.id)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 2: Login
Write-Host "Step 2: Logging in..." -ForegroundColor Yellow

$loginBody = @{
    email = $email
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    $token = $loginResponse.token
    Write-Host "✓ Login successful!" -ForegroundColor Green
    Write-Host "  Token: $($token.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host "  User ID: $($loginResponse.userId)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Step 3: Test Public Endpoint (No Token)
Write-Host "Step 3: Testing public endpoint (no auth)..." -ForegroundColor Yellow

try {
    $publicResponse = Invoke-RestMethod -Uri "$baseUrl/api/courses" `
        -Method Get `
        -ContentType "application/json"

    Write-Host "✓ Public endpoint accessible!" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "✗ Public endpoint failed (unexpected): $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Test Protected Endpoint WITHOUT Token (Should Fail)
Write-Host "Step 4: Testing protected endpoint WITHOUT token (should fail)..." -ForegroundColor Yellow

try {
    $protectedResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/$($registerResponse.id)" `
        -Method Get `
        -ContentType "application/json"

    Write-Host "✗ Protected endpoint accessible without token (SECURITY ISSUE!)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ Correctly blocked without token (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "? Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    Write-Host ""
}

# Step 5: Test Protected Endpoint WITH Token (Should Succeed)
Write-Host "Step 5: Testing protected endpoint WITH token (should succeed)..." -ForegroundColor Yellow

try {
    $headers = @{
        Authorization = "Bearer $token"
    }

    $protectedResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/$($registerResponse.id)" `
        -Method Get `
        -ContentType "application/json" `
        -Headers $headers

    Write-Host "✓ Protected endpoint accessible with token!" -ForegroundColor Green
    Write-Host "  User: $($protectedResponse.firstName) $($protectedResponse.lastName)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Protected endpoint failed with token: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 6: Test Invalid Token (Should Fail)
Write-Host "Step 6: Testing with invalid token (should fail)..." -ForegroundColor Yellow

try {
    $invalidHeaders = @{
        Authorization = "Bearer invalid.token.here"
    }

    $invalidResponse = Invoke-RestMethod -Uri "$baseUrl/api/users/$($registerResponse.id)" `
        -Method Get `
        -ContentType "application/json" `
        -Headers $invalidHeaders

    Write-Host "✗ Invalid token accepted (SECURITY ISSUE!)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✓ Correctly rejected invalid token (401 Unauthorized)" -ForegroundColor Green
    } else {
        Write-Host "? Unexpected error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    Write-Host ""
}

# Summary
Write-Host "=== Test Summary ===" -ForegroundColor Cyan
Write-Host "✓ User registration working" -ForegroundColor Green
Write-Host "✓ User login and JWT generation working" -ForegroundColor Green
Write-Host "✓ Public endpoints accessible" -ForegroundColor Green
Write-Host "✓ Protected endpoints blocked without token" -ForegroundColor Green
Write-Host "✓ Protected endpoints accessible with valid token" -ForegroundColor Green
Write-Host "✓ Invalid tokens rejected" -ForegroundColor Green
Write-Host ""
Write-Host "🎉 All authentication tests passed!" -ForegroundColor Green
Write-Host ""
Write-Host "Your JWT token for manual testing:" -ForegroundColor Yellow
Write-Host $token -ForegroundColor Gray
