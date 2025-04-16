package com.example.keycloak_wrapper.service

import com.example.keycloak_wrapper.dto.*

interface ICustomerService {
    fun getCustomers(): List<CustomerDto>
    fun getCustomer(id: String): CustomerDto
    fun createCustomer(dto: CustomerCreateDto): CustomerDto
    fun updateCustomer(id: String, dto: CustomerUpdateDto): CustomerDto
    fun deleteCustomer(id: String)
}
