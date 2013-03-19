package batch.demo.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Drawing implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private Date date;
	private String numbers;
	private int zz;
	private int s;
	private String spiel77;
	private String super6;
	private BigDecimal stake;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getNumbers() {
		return numbers;
	}
	public void setNumbers(String numbers) {
		this.numbers = numbers;
	}
	public int getZz() {
		return zz;
	}
	public void setZz(int zz) {
		this.zz = zz;
	}
	public int getS() {
		return s;
	}
	public void setS(int s) {
		this.s = s;
	}
	public String getSpiel77() {
		return spiel77;
	}
	public void setSpiel77(String spiel77) {
		this.spiel77 = spiel77;
	}
	public BigDecimal getStake() {
		return stake;
	}
	public void setStake(BigDecimal stake) {
		this.stake = stake;
	}
	public String getSuper6() {
		return super6;
	}
	public void setSuper6(String super6) {
		this.super6 = super6;
	}
	
	@Override
	public String toString() {
		return "Drawing [id=" + id + ", date=" + date + ", numbers=" + numbers
				+ ", zz=" + zz + ", s=" + s + ", spiel77=" + spiel77
				+ ", super6=" + super6 + ", stake=" + stake + "]";
	}
}
