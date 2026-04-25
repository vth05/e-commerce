# E-Commerce Application

## Overview
This is a comprehensive e-commerce application built using Java and Spring Boot. It provides a robust platform for managing products, orders, payments, and more. The application is designed to be scalable and maintainable, making it suitable for production environments.

## Features
- User authentication and authorization
- Product management
- Shopping cart functionality
- Order processing
- Payment integration
- Dashboard for analytics
- Email verification
- Inventory management
- Review and rating system
- Chatbot integration

## Application Flow
1. **User Registration and Login**:
   - Users can register an account and log in using their credentials.
   - Email verification is required to activate the account.

2. **Product Browsing and Search**:
   - Users can browse products by category or use the search functionality.

3. **Shopping Cart**:
   - Add products to the cart, update quantities, or remove items.

4. **Checkout and Payment**:
   - Users can proceed to checkout, select a payment method, and complete the purchase.

5. **Order Management**:
   - Users can view their order history and track the status of their orders.

6. **Chatbot Assistance**:
   - A chatbot is integrated to assist users with common queries and provide recommendations.

## Detailed Features
### Chatbot Integration
- The chatbot is designed to assist users in finding products based on specific criteria.
- Example: If a user asks, "Are there any products with at least 16GB RAM?", the chatbot will search the product database and provide links and basic information about matching products.
- Note: The chatbot does not currently handle order status inquiries or general FAQs.

### Payment Integration
- The application supports the following payment methods:
  - **VnPay**: Integrated for secure online transactions.
  - **Cash on Delivery (COD)**: Available for users who prefer offline payments.

### Dashboard
- Provides analytics for administrators, including sales data and user activity.

## Project Structure
The project follows a standard Spring Boot structure:

```
src/
  main/
    java/
      com/
        e_commerce/
          e_commerce/
            configuration/   # Configuration files
            controller/      # REST controllers
            dto/             # Data Transfer Objects
            entity/          # JPA entities
            enums/           # Enumerations
            exception/       # Custom exceptions
            mapper/          # Object mappers
            repository/      # Data repositories
            service/         # Business logic
            template/        # Email templates
            util/            # Utility classes
            validator/       # Custom validators
    resources/
      application.yaml       # Default application configuration
      application-prod.yml   # Production-specific configuration
      static/                # Static resources
      templates/             # Thymeleaf templates
  test/
    java/
      com/
        e_commerce/
          e_commerce/        # Test cases
```

## Prerequisites
- Java 21 or higher
- Maven 3.8+
- Docker (optional, for containerized deployment)

## Getting Started

### Clone the Repository
```bash
git clone https://github.com/vth05/e-commerce.git
cd e-commerce
```

### Build the Project
```bash
./mvnw clean install
```

### Run the Application
```bash
./mvnw spring-boot:run
```

### Access the Application
The application will be available at `http://localhost:8080`.

## Configuration
Configuration files are located in the `src/main/resources` directory. Update `application.yaml` or `application-prod.yml` as needed.

## Docker Deployment
To deploy the application using Docker:

1. Build the Docker image:
   ```bash
   docker build -t vinhth05/e-commerce:latest .
   ```

2. Run the Docker container:
   ```bash
   docker compose up -d
   ```

## Deployed Application
The application is deployed and accessible online. Use the following links to interact with the application:

- **Application Base URL**: [https://e-commerce-0-0-5.onrender.com](https://e-commerce-0-0-5.onrender.com)
- **Swagger API Documentation**: [https://e-commerce-0-0-5.onrender.com/swagger-ui/index.html](https://e-commerce-0-0-5.onrender.com/swagger-ui/index.html)

Note: The application currently does not have a frontend. You can use the Swagger UI to explore and test the available APIs.

## Contributing
Contributions are welcome! Please fork the repository and create a pull request.

## License
This project is licensed under the MIT License. See the LICENSE file for details.

## Contact
For any inquiries, please contact lhtvinh2005@gmail.com.
