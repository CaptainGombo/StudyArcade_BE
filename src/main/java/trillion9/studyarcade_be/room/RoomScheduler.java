package trillion9.studyarcade_be.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import trillion9.studyarcade_be.room.repository.RoomRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class RoomScheduler {
    private final RoomRepository roomRepository;

    @Autowired
    public RoomScheduler(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행되도록 설정
    public void deleteExpiredRooms() {
        LocalDate currentDate = LocalDate.now();
        List<Room> expiredRooms = roomRepository.findByExpirationDateBefore(currentDate);

        roomRepository.deleteAll(expiredRooms);
    }
}
