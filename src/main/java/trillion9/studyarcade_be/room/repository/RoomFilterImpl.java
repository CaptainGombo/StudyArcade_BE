package trillion9.studyarcade_be.room.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import trillion9.studyarcade_be.room.QRoom;
import trillion9.studyarcade_be.room.dto.RoomResponseDto;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomFilterImpl implements RoomFilter {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RoomResponseDto> findAllRoom(Pageable pageable) {
        List<RoomResponseDto> roomResponseDtos = queryFactory.select(Projections.constructor(
                RoomResponseDto.class, QRoom.room))
                .from(QRoom.room)
                .orderBy(QRoom.room.createdAt.desc()) // 최신 순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(roomResponseDtos, pageable, roomResponseDtos.size());
    }
}
