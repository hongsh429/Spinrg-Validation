package hello.itemservice.domain.item;

import lombok.Data;
/* 이건 하이버네이트 validator 를 사용할 때만 사용 가능 */
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;

/* 특정 구현체에 상관없는 표준 인터페이스 */
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Item {

    private Long id;

    @NotBlank(message = "공백 X")
    private String itemName;

    @NotNull
    @Range(min = 1000, max = 1000000)
    private Integer price;

    @NotNull
    @Max(9999)
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
