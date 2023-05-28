package me.chacham.dongbaek.infra.grpc

import me.chacham.dongbaek.domain.schedule.ProgressRepository
import me.chacham.dongbaek.domain.schedule.ScheduleId
import me.chacham.dongbaek.infra.proto.PbUtils.toInstant
import me.chacham.dongbaek.infra.proto.PbUtils.toPbProgress
import me.chacham.dongbaek.infra.proto.PbUtils.toPbTimestamp
import me.chacham.dongbaek.infra.proto.PbUtils.toProgress
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class ProgressGrpcServiceImpl(val progressRepository: ProgressRepository) :
    ProgressServiceGrpcKt.ProgressServiceCoroutineImplBase() {
    override suspend fun getProgresses(request: GetProgressesRequest): GetProgressesResponse {
        val scheduleIds = request.scheduleIdsList.map { ScheduleId(it) }
        val instant = request.datetime.toInstant()
        val progresses = progressRepository.list(scheduleIds, instant)
        val pbProgresses = progresses.map { it.toPbProgress() }
        return GetProgressesResponse.newBuilder()
            .addAllProgresses(pbProgresses)
            .build()
    }

    override suspend fun replaceProgress(request: ReplaceProgressRequest): ReplaceProgressResponse {
        val progress = request.progress.toProgress()
        progressRepository.save(progress)
        return replaceProgressResponse {
            scheduleId = progress.scheduleId.value
            startTimestamp = progress.startDate.toPbTimestamp()
        }
    }
}
