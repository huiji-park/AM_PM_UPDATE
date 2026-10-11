package phoenix.AM_PM.domain.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import phoenix.AM_PM.domain.project.dto.RequestProject;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private LocalDate startDate;
  private LocalDate endDate;
  private String title;
  private String content;

  public static Project from(RequestProject requestProject) {
    return new Project(null,
            requestProject.getStartDate(),
            requestProject.getEndDate(),
            requestProject.getTitle(),
            requestProject.getContent());
  }

  public void updateTitle(String title) {
    this.title = title;
  }
  public void updateContent(String content) {
    this.content = content;
  }
  public void updateStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }
  public void updateEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  // Constructors, getters, and setters
}