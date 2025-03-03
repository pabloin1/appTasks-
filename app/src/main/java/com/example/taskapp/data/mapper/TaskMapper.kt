package com.example.taskapp.data.mapper

import com.example.taskapp.data.model.Task as DataTask
import com.example.taskapp.data.remote.dto.TaskDto
import com.example.taskapp.domain.model.Task as DomainTask

fun TaskDto.toDomainTask(): DomainTask {
    return DomainTask(
        id = _id,
        title = title,
        description = description,
        completed = Completed,
        userId = userId,
        createdAt = try {
            // Parsear createdAt si es posible, o usar tiempo actual
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    )
}

fun DataTask.toDomainTask(): DomainTask {
    return DomainTask(
        id = _id,
        title = title,
        description = description,
        completed = Completed,
        userId = userId,
        createdAt = createdAt
    )
}

fun DomainTask.toDataTask(): DataTask {
    return DataTask(
        _id = id,
        title = title,
        description = description,
        Completed = completed,
        userId = userId,
        createdAt = createdAt
    )
}

fun List<TaskDto>.toDomainTaskList(): List<DomainTask> {
    return this.map { it.toDomainTask() }
}

fun DataTask.toCreateTaskRequest(): com.example.taskapp.data.remote.dto.CreateTaskRequest {
    return com.example.taskapp.data.remote.dto.CreateTaskRequest(
        title = title,
        description = description
    )
}