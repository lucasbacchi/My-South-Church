# deploy.ps1

# 1. Get the token safely into a variable
Write-Host "Getting Google Access Token..."
$token = gcloud.cmd auth print-access-token

# 2. Check if we actually got a token
if (-not $token) {
    Write-Error "Failed to get token from gcloud."
    exit 1
}

# 3. Build the Container with Maven
Write-Host "Building Container Image..."
mvn compile jib:build "-Djib.to.auth.username=oauth2accesstoken" "-Djib.to.auth.password=$token"

# Check if build failed
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed."
    exit 1
}

# 4. Deploy to Cloud Run
Write-Host "Deploying to Google Cloud Run..."
gcloud.cmd run deploy backend-api `
  --image gcr.io/my-south-church/backend-api `
  --platform managed `
  --region us-east1 `
  --allow-unauthenticated `
  --set-secrets="GOOGLE_CREDENTIALS_JSON=GOOGLE_CREDENTIALS_JSON:latest" `
  --set-secrets="SPRING_SECURITY_USER_NAME=SPRING_SECURITY_USER_NAME:latest" `
  --set-secrets="SPRING_SECURITY_USER_PASSWORD=SPRING_SECURITY_USER_PASSWORD:latest" `
  --set-secrets="GOOGLE_ROOT_GROUP_EMAIL=GOOGLE_ROOT_GROUP_EMAIL:latest" `
  --set-secrets="DB_URL=DB_URL:latest" `
  --set-secrets="DB_USERNAME=DB_USERNAME:latest" `
  --set-secrets="DB_PASSWORD=DB_PASSWORD:latest" `
  --set-secrets="/app/certs/truststore.jks=AIVEN_TRUSTSTORE:latest"