package com.example.keycloak_wrapper.controller

import com.example.keycloak_wrapper.dto.*
import com.example.keycloak_wrapper.util.SecurityContextHelper
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: ICustomerService,
    private val securityContextHelper: SecurityContextHelper
) {
    @GetMapping
    @Operation(
        summary = "Get all customers",
        description = "Returns all customers. Only system admins can see all customers."
    )
    fun getCustomers(): ResponseEntity<ApiResponse<List<CustomerDto>>> {
        val customers = customerService.getCustomers()
        return ResponseEntity.ok(ApiResponse(success = true, data = customers))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get customer by ID",
        description = "Returns a specific customer by ID."
    )
    fun getCustomer(
        @Parameter(description = "ID of the customer to retrieve", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<CustomerDto>> {
        return customerService.getCustomer(id).ok()
    }

    @PostMapping
    @Operation(
        summary = "Create customer",
        description = "Creates a new customer. Only system administrators can create customers."
    )
    fun createCustomer(
        @Parameter(required = true)
        @RequestBody customerCreateDto: CustomerCreateDto
    ): ResponseEntity<ApiResponse<CustomerDto>> {
        // Validate customer name uniqueness
        val existingCustomers = customerService.getCustomers()
        val customerExists = existingCustomers.any { it.name == customerCreateDto.name }
        if (customerExists) {
            return ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    error = "Customer with name '${customerCreateDto.name}' already exists"
                )
            )
        }
        return customerService.createCustomer(customerCreateDto).created()
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update customer",
        description = "Updates a customer's display name."
    )
    fun updateCustomer(
        @Parameter(description = "ID of the customer to update", required = true)
        @PathVariable id: String,
        @Parameter(description = "Customer update details", required = true)
        @RequestBody customerUpdateDto: CustomerUpdateDto
    ): ResponseEntity<ApiResponse<CustomerDto>> {
        return customerService.updateCustomer(id, customerUpdateDto).ok()
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete customer",
        description = "Deletes a customer and all its tenants and groups. Only system administrators can delete customers."
    )
    @ApiResponses(
        SwaggerResponse(responseCode = "200", description = "Customer successfully deleted"),
        SwaggerResponse(responseCode = "401", description = "Unauthorized"),
        SwaggerResponse(responseCode = "403", description = "Forbidden - user is not a system admin"),
        SwaggerResponse(responseCode = "404", description = "Customer not found")
    )
    fun deleteCustomer(
        @Parameter(description = "ID of the customer to delete", required = true)
        @PathVariable id: String
    ): ResponseEntity<ApiResponse<Unit>> {
        customerService.deleteCustomer(id)
        return Unit.ok()
    }
}
