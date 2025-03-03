package com.example.taskapp.data.mapper

import com.example.taskapp.data.model.User as DataUser
import com.example.taskapp.data.remote.dto.UserDto
import com.example.taskapp.domain.model.User as DomainUser

fun UserDto.toDomainUser(): DomainUser {
    return DomainUser(
        id = _id,
        name = name,
        email = email,
        token = token ?: ""
    )
}

fun DataUser.toDomainUser(): DomainUser {
    return DomainUser(
        id = _id,
        name = name,
        email = email,
        token = token
    )
}

fun DomainUser.toDataUser(): DataUser {
    return DataUser(
        _id = id,
        name = name,
        email = email,
        password = "", // No almacenamos la contraseña
        token = token
    )
}