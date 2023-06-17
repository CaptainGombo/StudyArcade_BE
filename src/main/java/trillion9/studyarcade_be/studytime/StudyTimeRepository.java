package trillion9.studyarcade_be.studytime;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

    @Query(value = "SELECT s.daily_date as date, s.daily_study_time as dailyStudyTime FROM study_time as s " +
            "WHERE s.member_id = :memberId AND s.daily_date >= :startDate AND s.daily_date <= :endDate ORDER BY s.daily_date", nativeQuery = true)
    List<Object[]> findStudyTimeByDateRange(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT WEEK(s.dailyDate) as week, SUM(s.dailyStudyTime) as totalStudyTime FROM StudyTime s " +
            "WHERE s.memberId = :memberId AND s.dailyDate >= :startDate AND s.dailyDate <= :endDate " +
            "GROUP BY WEEK(s.dailyDate) ORDER BY WEEK(s.dailyDate)")
    List<Object[]> findStudyTimeByWeekRange(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT YEAR(s.dailyDate) as year, MONTH(s.dailyDate) as month, SUM(s.dailyStudyTime) as totalStudyTime FROM StudyTime s " +
            "WHERE s.memberId = :memberId AND s.dailyDate >= :startDate AND s.dailyDate <= :endDate " +
            "GROUP BY YEAR(s.dailyDate), MONTH(s.dailyDate) ORDER BY YEAR(s.dailyDate), MONTH(s.dailyDate)")
    List<Object[]> findStudyTimeByMonthRange(@Param("memberId") Long memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
