package phoenix.AM_PM.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import phoenix.AM_PM.domain.project.entity.Project;
import phoenix.AM_PM.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString
public class Chat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne
  @JoinColumn(name = "project_id")
  private Project project;

  @Column(nullable = false)
  private String message;

  @CreatedDate
  @Column(name = "created_date")
  private LocalDateTime createdDate;

  private Long unread;

  @Column(name = "who_read")
  private String whoRead;

  // setter 대신 update
  public void updateUnread(Long unread) {
    this.unread = unread;
  }

  public void updateWhoRead(String whoRead) {
    this.whoRead = whoRead;
  }
}