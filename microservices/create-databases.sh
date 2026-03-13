#!/bin/bash

# Script to create all microservices databases

echo "Creating microservices databases..."

# Database credentials
DB_USER="postgres"
DB_PASSWORD="postgres"
DB_HOST="localhost"
DB_PORT="5432"

# Create databases
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE user_management_db;"
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE wallet_management_db;"
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE transaction_management_db;"
psql -U $DB_USER -h $DB_HOST -p $DB_PORT -c "CREATE DATABASE payment_management_db;"

echo "All databases created successfully!"
echo ""
echo "Databases:"
echo "  - user_management_db (port 5432)"
echo "  - wallet_management_db (port 5433)"
echo "  - transaction_management_db (port 5434)"
echo "  - payment_management_db (port 5435)"
