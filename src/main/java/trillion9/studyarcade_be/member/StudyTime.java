package trillion9.studyarcade_be.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyTime {
    @Id
    private Long memberId;

    @Column
    private LocalDate dailyDate;

    @Column
    private Long dailyStudyTime;
}

