package phoenix.AM_PM.domain.task.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task")
@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private int projectId;
  private String Nickname;
  private LocalDateTime beginDate;
  private LocalDateTime endDate;
  private String content;
  private String backgroundColor;
  private String frontColor;
}