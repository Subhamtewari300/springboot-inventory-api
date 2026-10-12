package dev.subham.inventory;

import jakarta.persistence.*;

@Entity
@Table(name="reservations", uniqueConstraints=@UniqueConstraint(columnNames="idempotency_key"))
public class Reservation {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) private Product product;
  @Column(nullable=false) private int quantity;
  @Column(name="idempotency_key",nullable=false,unique=true) private String idempotencyKey;
  @Column(nullable=false) private boolean cancelled;

  protected Reservation() {}
  public Reservation(Product product,int quantity,String key){this.product=product;this.quantity=quantity;this.idempotencyKey=key;}
  public Long getId(){return id;}
  public Long getProductId(){return product.getId();}
  public int getQuantity(){return quantity;}
  public String getIdempotencyKey(){return idempotencyKey;}
  public boolean isCancelled(){return cancelled;}
  public void cancel(){cancelled=true;}
}
