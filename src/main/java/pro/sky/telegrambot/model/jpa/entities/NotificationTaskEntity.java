package pro.sky.telegrambot.model.jpa.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_task")
public class NotificationTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Generated(value = GenerationTime.INSERT)
    private Integer id;

    @Column(name = "chat_id")
    private @NotNull String chatId;

    @Column(name = "task")
    private @NotNull String task;

    @Column(name = "date")
    private @NotNull LocalDateTime date;

    @Column(name = "send_status")
    private Boolean sendStatus;

    @Override
    public String toString() {
        return  task +
                " на дату " + date;
    }
}
