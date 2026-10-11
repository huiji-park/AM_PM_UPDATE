package phoenix.AM_PM.domain.members.entity;

import jakarta.persistence.*;
import lombok.*;
import phoenix.AM_PM.domain.project.entity.Project;
import phoenix.AM_PM.domain.user.entity.User;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Builder
@Getter
public class Members {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "project_id")
  private Project project;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(value = EnumType.STRING)
  private Roles roles;

  public void updateRoles(Roles roles) {
    this.roles = roles;
  }
  // Constructors, getters, and setters
}
