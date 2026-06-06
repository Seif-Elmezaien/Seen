package com.example.seen.domain.model.logs

data class LogResponse(
    val upserted_logs : List<LogRequest>,
    val deleted_log_ids : List<String>,
)
