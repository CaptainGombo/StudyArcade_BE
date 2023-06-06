package trillion9.studyarcade_be.room.repository;

import com.querydsl.core.BooleanBuilder;
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
    public Page<RoomResponseDto> findRooms(Pageable pageable, String category, String keyword) {
        QRoom room = QRoom.room;

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (category != null && !category.isEmpty()) {
            String[] categories = category.split(",");
            for (String cat : categories) {
                booleanBuilder.or(room.categories.contains(cat));
            }
        }

        if (keyword != null && !keyword.isEmpty()) {
            String[] keywords = keyword.split("\\s+|[^\\p{IsAlphabetic}\\p{IsDigit}]+");
            for (String key : keywords) {
                booleanBuilder.or(room.roomName.contains(key))
                              .or(room.roomContent.contains(key));
            }
        }

        List<RoomResponseDto> roomResponseDtos = queryFactory.select(Projections.constructor(
                RoomResponseDto.class, room))
                .from(room)
                .orderBy(room.createdAt.desc()) // 최신 순
                .where(booleanBuilder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(roomResponseDtos, pageable, roomResponseDtos.size());
    }

}
