package trillion9.studyarcade_be.studytime;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

    @Query(value = "SELECT s.dailyDate as date, s.dailyStudyTime as dailyStudyTime FROM StudyTime s " +
            "WHERE s.memberId = :memberId AND s.dailyDate >= :startDate AND s.dailyDate <= :endDate ORDER BY s.dailyDate", nativeQuery = true)
    List<Object[]> findStudyTimeByDateRange(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT WEEK(s.dailyDate) as week, SUM(s.dailyStudyTime) as totalStudyTime FROM StudyTime s " +
            "WHERE s.memberId = :memberId AND s.dailyDate >= :startDate AND s.dailyDate <= :endDate " +
            "GROUP BY WEEK(s.dailyDate) ORDER BY WEEK(s.dailyDate)", nativeQuery = true)
    List<Object[]> findStudyTimeByWeekRange(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT YEAR(s.dailyDate) as year, MONTH(s.dailyDate) as month, SUM(s.dailyStudyTime) as totalStudyTime FROM StudyTime s " +
            "WHERE s.memberId = :memberId AND s.dailyDate >= :startDate AND s.dailyDate <= :endDate " +
            "GROUP BY YEAR(s.dailyDate), MONTH(s.dailyDate) ORDER BY YEAR(s.dailyDate), MONTH(s.dailyDate)", nativeQuery = true)
    List<Object[]> findStudyTimeByMonthRange(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
