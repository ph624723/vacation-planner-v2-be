package com.ph.persistence.model;

import com.ph.model.TimeSpan;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Date;

@Entity
@Table(name = "absence")
public class AbsenceEntity {

	@Id
	@GeneratedValue
	@Getter
	@Setter
	private Long id;

	@Getter
	@Setter
	@ManyToOne
	@JoinColumn(name = "user_name")
	private PersonEntity person;

	@Getter
	@Setter
	@Type(type = "text")
	@Lob
	private String description;

	@Getter
	@Setter
	private java.sql.Date startDate;

	@Getter
	@Setter
	private java.sql.Date endDate;

	@Getter
	@Setter
	private int level;

	@Getter
	@Setter
	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Getter
	@Setter
	@UpdateTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	public Importance getImportance() {
		return Importance.fromInt(level);
	}

	public void setImportance(Importance importance){
		level = importance.getLevel();
	}

	public TimeSpan asTimeSpan(){
		return new TimeSpan(startDate,endDate,description);
	}

	public AbsenceEntity trimDescription(){
		if(description.length() > 20) {
			description = description.substring(0, 17) + "...";
		}
		return this;
	}


	public enum Importance {
		High(3),
		Medium(2),
		Low(1),
		Irrelevant(0);

		@Getter
		private final int level;

		private Importance(int level){
			this.level = level;
		}

		public static Importance fromInt(int input){
			return Arrays.stream(Importance.values()).filter(x -> x.getLevel() == input).findAny().orElse(null);
		}
	}

	
}