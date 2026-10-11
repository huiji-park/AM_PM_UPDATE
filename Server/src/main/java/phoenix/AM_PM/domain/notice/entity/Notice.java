package phoenix.AM_PM.domain.notice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notice")
@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private int projectId;
  private String userId;
  private String content;
  private String title;
}