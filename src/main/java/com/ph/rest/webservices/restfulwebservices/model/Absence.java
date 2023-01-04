package com.ph.rest.webservices.restfulwebservices.model;

import com.ph.model.TimeSpan;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;

@ApiModel(description = "Meant to store absent times for individual users where no further plans are desired. \n" +
						"Includes an importance level from 0 (irrelevant) to 3 (high) to indicate the strictness of unavailability.")
@Entity
public class Absence {
	@ApiModelProperty(position = 0, value = "", required = false)
	@Id
	@GeneratedValue
	@Getter
	@Setter
	private Long id;
	@ApiModelProperty(position = 1, required = true)
	@Getter
	@Setter
	@ManyToOne
	@JoinColumn(name = "user_name")
	private User user;
	@ApiModelProperty(position = 5, required = false, value = "Vacation without you.")
	@Getter
	@Setter
	private String description;
	@ApiModelProperty(position = 2, required = true, value = "1993-05-12")
	@Getter
	@Setter
	private java.sql.Date startDate;
	@ApiModelProperty(position = 3, required = true, value = "1993-05-24")
	@Getter
	@Setter
	private java.sql.Date endDate;
	@ApiModelProperty(position = 4, required = true, value = "2")
	@Getter
	@Setter
	private int level;

	@ApiModelProperty(hidden = true)
	public Importance getImportance() {
		return Importance.fromInt(level);
	}

	public void setImportance(Importance importance){
		level = importance.getLevel();
	}

	public TimeSpan asTimeSpan(){
		return new TimeSpan(startDate,endDate);
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