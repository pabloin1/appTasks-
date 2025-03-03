package com.example.taskapp.data.mapper

import com.example.taskapp.data.model.Result as DataResult
import com.example.taskapp.domain.model.Result as DomainResult

fun <T, R> DataResult<T>.toDomainResult(transform: (T) -> R): DomainResult<R> {
    return when (this) {
        is DataResult.Success -> DomainResult.Success(transform(data))
        is DataResult.Error -> DomainResult.Error(exception)
        is DataResult.Loading -> DomainResult.Loading
    }
}

fun <T, R> DomainResult<T>.toDataResult(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DomainResult.Success -> DataResult.Success(transform(data))
        is DomainResult.Error -> DataResult.Error(exception)
        is DomainResult.Loading -> DataResult.Loading
    }
}