# Azure Web Apps Deployment Guide

This guide provides detailed instructions for deploying the Sports Coaching Platform microservices to Azure Web Apps using the Azure Portal.

## Architecture Overview

The platform consists of 3 microservices:
- **coach-service** (port 8081) - Manages coach profiles and ratings
- **session-service** (port 8082) - Manages coaching sessions and users
- **review-service** (port 8083) - Handles review submissions

## Prerequisites

### Azure Resources Required
1. **Azure Subscription** with appropriate permissions
2. **Azure SQL Database** - Single server with 3 databases
3. **Azure Storage Account** - For queue messaging
4. **Azure Web Apps** - 3 App Service instances
5. **Azure DevOps** - For CI/CD pipeline

### Local Development Requirements
- Java 17 or later
- Maven 3.8+

## Step 1: Create Resource Group

1. Log in to [Azure Portal](https://portal.azu
2. re.com)
2. Click **"Create a resource"** in the top left
3. Search for **"Resource group"** and click **Create**
4. Fill in the details:
   - **Subscription**: Select your subscription
   - **Resource group**: `sdc-sports-coaching-rg`
   - **Region**: Select your preferred region (e.g., East US)
5. Click **"Review + create"**, then **"Create"**

## Step 2: Create Azure SQL Server and Databases

### 2.1 Create SQL Server

1. Click **"Create a resource"**
2. Search for **"Azure SQL"** and click **Create**
3. Select **"SQL databases"** > **"Single database"** > **Create**
4. In the **Basics** tab:
   - **Subscription**: Select your subscription
   - **Resource group**: `sdc-sports-coaching-rg`
   - **Database name**: `coach_db`
   - **Server**: Click **"Create new"**
     - **Server name**: `sdc-sql-server` (must be globally unique)
     - **Location**: Same as resource group
     - **Authentication method**: Use SQL authentication
     - **Server admin login**: `sqladmin`
     - **Password**: Create a strong password
     - Click **OK**
   - **Compute + storage**: Click **"Configure database"**
     - Select **Basic** or **Standard S0** for development
     - Click **Apply**
5. Click **"Review + create"**, then **"Create"**

### 2.2 Create Additional Databases

Repeat the database creation for:
- `session_db`
- `review_db`

Use the same SQL server created above.

### 2.3 Configure Firewall Rules

1. Navigate to your SQL Server (`sdc-sql-server`)
2. In the left menu, click **"Networking"** under Security
3. Under **"Firewall rules"**:
   - Toggle **"Allow Azure services and resources to access this server"** to **Yes**
4. Click **Save**

## Step 3: Create Storage Account and Queues

### 3.1 Create Storage Account

1. Click **"Create a resource"**
2. Search for **"Storage account"** and click **Create**
3. In the **Basics** tab:
   - **Subscription**: Select your subscription
   - **Resource group**: `sdc-sports-coaching-rg`
   - **Storage account name**: `sdcstorageaccount` (must be globally unique, lowercase only)
   - **Region**: Same as resource group
   - **Performance**: Standard
   - **Redundancy**: Locally-redundant storage (LRS)
4. Click **"Review + create"**, then **"Create"**

### 3.2 Create Queues

1. Navigate to your storage account
2. In the left menu, click **"Queues"** under Data storage
3. Click **"+ Queue"** and create:
   - `review-events`
   - `app-logs`

### 3.3 Get Connection String

1. In the storage account, click **"Access keys"** under Security + networking
2. Click **"Show"** next to key1
3. Copy the **Connection string** - you'll need this later

## Step 4: Create App Service Plan and Web Apps

### 4.1 Create App Service Plan

1. Click **"Create a resource"**
2. Search for **"App Service Plan"** and click **Create**
3. In the **Basics** tab:
   - **Subscription**: Select your subscription
   - **Resource group**: `sdc-sports-coaching-rg`
   - **Name**: `sdc-app-service-plan`
   - **Operating System**: Linux
   - **Region**: Same as resource group
   - **Pricing plan**: Select **Basic B1** for development or **Standard S1** for production
4. Click **"Review + create"**, then **"Create"**

### 4.2 Create Web Apps

Create 3 Web Apps for each service:

#### Coach Service
1. Click **"Create a resource"**
2. Search for **"Web App"** and click **Create**
3. In the **Basics** tab:
   - **Subscription**: Select your subscription
   - **Resource group**: `sdc-sports-coaching-rg`
   - **Name**: `sdc-coach-service` (must be globally unique)
   - **Publish**: Code
   - **Runtime stack**: Java 17
   - **Java web server stack**: Java SE (Embedded Web Server)
   - **Operating System**: Linux
   - **Region**: Same as resource group
   - **App Service Plan**: Select `sdc-app-service-plan`
4. Click **"Review + create"**, then **"Create"**

Repeat for:
- **Session Service**: Name = `sdc-session-service`
- **Review Service**: Name = `sdc-review-service`

## Step 5: Configure Application Settings

### 5.1 Coach Service Configuration

1. Navigate to your `sdc-coach-service` Web App
2. In the left menu, click **"Configuration"** under Settings
3. Under **"Application settings"**, click **"+ New application setting"** for each:

| Name                              | Value                                 |
|-----------------------------------|---------------------------------------|
| `AZURE_SQL_SERVER`                | `sdc-sql-server.database.windows.net` |
| `AZURE_SQL_DATABASE_COACH`        | `coach_db`                            |
| `AZURE_SQL_USERNAME`              | `sqladmin`                            |
| `AZURE_SQL_PASSWORD`              | `<your-password>`                     |
| `AZURE_STORAGE_CONNECTION_STRING` | `<your-storage-connection-string>`    |
| `SERVER_PORT`                     | `80`                                  |
| `JAVA_OPTS`                       | `-Xms256m -Xmx512m`                   |

4. Click **Save**

### 5.2 Session Service Configuration

Navigate to `sdc-session-service` and add:

| Name                              | Value                                         |
|-----------------------------------|-----------------------------------------------|
| `AZURE_SQL_SERVER`                | `sdc-sql-server.database.windows.net`         |
| `AZURE_SQL_DATABASE_SESSION`      | `session_db`                                  |
| `AZURE_SQL_USERNAME`              | `sqladmin`                                    |
| `AZURE_SQL_PASSWORD`              | `<your-password>`                             |
| `AZURE_STORAGE_CONNECTION_STRING` | `<your-storage-connection-string>`            |
| `COACH_SERVICE_URL`               | `https://sdc-coach-service.azurewebsites.net` |
| `SERVER_PORT`                     | `80`                                          |
| `JAVA_OPTS`                       | `-Xms256m -Xmx512m`                           |

### 5.3 Review Service Configuration

Navigate to `sdc-review-service` and add:

| Name                              | Value                              |
|-----------------------------------|------------------------------------|
| `AZURE_STORAGE_CONNECTION_STRING` | `<your-storage-connection-string>` |
| `SERVER_PORT`                     | `80`                               |
| `JAVA_OPTS`                       | `-Xms256m -Xmx256m`                |

### 5.4 Configure Health Check (Optional)

For each Web App:
1. Go to **"Health check"** under Monitoring
2. Enable Health check
3. Set Path to `/health`
4. Click **Save**

## Step 6: Build and Package Applications

On your local machine:

```bash
# From project root directory
mvn clean package -DskipTests
```

This produces:
- `coach-service/target/coach-service-1.0-SNAPSHOT.jar`
- `session-service/target/session-service-1.0-SNAPSHOT.jar`
- `review-service/target/review-service-1.0-SNAPSHOT.jar`

## Step 7: Deploy to Azure Web Apps

### Option A: Deploy via Azure Portal

For each service:

1. Navigate to the Web App (e.g., `sdc-coach-service`)
2. In the left menu, click **"Deployment Center"** under Deployment
3. Select **"Local Git"** or **"GitHub"** as source
4. For manual upload:
   - Click **"Advanced Tools"** under Development Tools
   - Click **"Go"** to open Kudu
   - Navigate to **Debug console** > **CMD**
   - Go to `site/wwwroot`
   - Drag and drop your JAR file

### Option B: Deploy via Azure DevOps Pipeline (Recommended)

The project includes an `azure-pipelines.yml` that automates deployment.

#### Set Up Azure DevOps

1. Go to [Azure DevOps](https://dev.azure.com)
2. Create a new project or use existing
3. Go to **Pipelines** > **Create Pipeline**
4. Select your repository source
5. Select **Existing Azure Pipelines YAML file**
6. Select `/azure-pipelines.yml`

#### Create Service Connection

1. Go to **Project Settings** > **Service connections**
2. Click **"New service connection"**
3. Select **"Azure Resource Manager"**
4. Choose **"Service principal (automatic)"**
5. Select your subscription and resource group
6. Name it (e.g., `sdc-azure-connection`)
7. Click **Save**

#### Configure Pipeline Variables

1. Go to your pipeline > **Edit** > **Variables**
2. Add:
   - `azureSubscription`: `sdc-azure-connection`
   - `resourceGroup`: `sdc-sports-coaching-rg`
   - `coachServiceName`: `sdc-coach-service`
   - `sessionServiceName`: `sdc-session-service`
   - `reviewServiceName`: `sdc-review-service`

3. Click **Save**

## Step 8: Verify Deployment

### 8.1 Check Application Health

Open a browser and navigate to:
- `https://sdc-coach-service.azurewebsites.net/health`
- `https://sdc-session-service.azurewebsites.net/health`
- `https://sdc-review-service.azurewebsites.net/health`

Each should return:
```json
{
  "status": "UP",
  "service": "<service-name>",
  "timestamp": "..."
}
```

### 8.2 View Application Logs

1. Navigate to the Web App
2. Click **"Log stream"** under Monitoring
3. View real-time logs

### 8.3 Access Swagger UI

- Coach Service: `https://sdc-coach-service.azurewebsites.net/swagger-ui.html`
- Session Service: `https://sdc-session-service.azurewebsites.net/swagger-ui.html`
- Review Service: `https://sdc-review-service.azurewebsites.net/swagger-ui.html`

## Troubleshooting

### Common Issues

#### 1. Application Won't Start
- Go to **"Log stream"** to view startup logs
- Check **"Diagnose and solve problems"** for automated diagnostics
- Verify all environment variables are set correctly in Configuration

#### 2. Database Connection Failures
- Verify SQL Server firewall allows Azure services
- Check connection string format in Configuration
- Ensure database exists and credentials are correct

#### 3. Queue Communication Issues
- Verify storage connection string is correct
- Check queue names match configuration
- Ensure queues exist in storage account

#### 4. Service-to-Service Communication
- Verify `COACH_SERVICE_URL` is correct for session-service
- Check network connectivity between services
- Ensure target service is healthy

### Restart Application

1. Navigate to the Web App
2. Click **"Restart"** in the top toolbar
3. Wait for the application to restart

### Scale Application

1. Navigate to the Web App
2. Click **"Scale up (App Service plan)"** under Settings
3. Select a different pricing tier
4. Click **Apply**

## Security Best Practices

1. **Use Azure Key Vault** for secrets
   - Navigate to **"Key Vault"** and create a new vault
   - Store database passwords and connection strings
   - Reference secrets in App Configuration

2. **Enable Managed Identity**
   - Go to Web App > **"Identity"** under Settings
   - Enable **System assigned** identity
   - Grant access to Key Vault and other resources

3. **Configure SSL/TLS**
   - Go to **"TLS/SSL settings"** under Settings
   - Enable **HTTPS Only**
   - Configure custom domain with certificate if needed

## Monitoring

### Enable Application Insights

1. Navigate to the Web App
2. Click **"Application Insights"** under Settings
3. Click **"Turn on Application Insights"**
4. Select or create an Application Insights resource
5. Click **Apply**

### View Metrics

1. Navigate to the Web App
2. Click **"Metrics"** under Monitoring
3. Select metrics like:
   - CPU Percentage
   - Memory Percentage
   - HTTP Server Errors
   - Response Time

### Prometheus Metrics

Each service exposes metrics at `/actuator/prometheus` for integration with external monitoring systems.

## Cost Optimization

1. Use **B1** tier for development, **S1** or higher for production
2. Consider **autoscaling** based on load:
   - Go to **"Scale out (App Service plan)"**
   - Configure rules based on CPU or memory
3. Use **reserved capacity** for databases in production
4. Set up **cost alerts** in Azure Cost Management

## Next Steps

1. Configure custom domains
2. Set up SSL certificates
3. Implement blue-green deployments using deployment slots
4. Configure auto-scaling rules
5. Set up alerts and monitoring dashboards
