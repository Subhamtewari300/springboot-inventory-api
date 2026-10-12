package dev.subham.inventory;

import jakarta.persistence.*;

@Entity
@Table(name="products", uniqueConstraints=@UniqueConstraint(columnNames="sku"))
public class Product {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true) private String sku;
  @Column(nullable=false) private String name;
  @Column(nullable=false) private int stock;

  protected Product() {}
  public Product(String sku,String name,int stock) { this.sku=sku; this.name=name; this.stock=stock; }
  public Long getId(){return id;}
  public String getSku(){return sku;}
  public String getName(){return name;}
  public int getStock(){return stock;}
  public void reserve(int qty){this.stock-=qty;}
  public void release(int qty){this.stock+=qty;}
}
