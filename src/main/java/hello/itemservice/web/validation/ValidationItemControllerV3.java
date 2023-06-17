package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;


@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;


    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }


    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes
    ) {

       /*
       특정 필드가 아닌 복합 룰 검증은 다음을 쓰는게 낫다!
          -  new ObjectError! 의 경우, reject() ✨✨
          -  new FieldError의 경우 rejectValue()
       */
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            /* bindResult는 view 로 알아서 가지고 간다.*/
            return "validation/v3/addForm";
        }

        // 아래는 성공 로직!
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId,
                       @Validated @ModelAttribute Item item,
                       BindingResult bindingResult) {

        // 특정 필드 검증이 아닌 복합적 검증
        if (item.getQuantity() != null && item.getPrice() != null) {
            int total = item.getQuantity() * item.getPrice();
            if (total < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, total}, null);
            }
        }

        // 이제 validation 과정이 마치고 에러가 있엇는지 없었는지 확인하고 페이지를 다시 원래 수정페이지로 값을 가지고 간다
        if (bindingResult.hasErrors()) {
            log.info("error={}", bindingResult);
            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

