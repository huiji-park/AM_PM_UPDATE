package phoenix.AM_PM.domain.projectplan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projectPlan")
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProjectPlan {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private int id; // 프로젝트기획ID
  private int projectId;     // 프로젝트ID
  private String title; // 분류
  private String filePath; // 파일경로
  private String sampleUrl; // 예시 url
  private String sampleImg; // 예시 image

}