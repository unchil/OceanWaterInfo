package com.unchil.oceanwaterinfo.data

enum class RepositoryLogHeader{
    ServingFromCache, ServingFromDb
}
enum class ApplicationLogHeader {
    Service_Call, Respond_NotFound, Respond_Data, Respond_Error, Respond_BadRequest
}
