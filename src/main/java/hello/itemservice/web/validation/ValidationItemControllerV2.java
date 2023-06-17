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
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    // 웹에서 전달받은 데이터를 binding하게 해주는 객체
    // 하위 어떤 handler mapping이 되더라도, 이 로직은 항상 먼저 처리된다.
    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(itemValidator);

    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
                                /*
                                form tag의 name과 binding 된다!@
                                이 과정에서 에러가 발생하면, BindingResult 가 동작하면서
                                검증 오류에 대해서 new FieldError / new ObjectError 를 BindingResult에 담아서
                                view에 보내준다.
                                */
    public String addItemV1(@ModelAttribute Item item,
                          BindingResult bindingResult,  /* 무조건 ModelAttribute 객체 다음에 와야한다 */
                          RedirectAttributes redirectAttributes
    ) {

        //검증 로직
        // 필드 오류!!! Item 필드에 대한 오류 new FieldError
        if (!StringUtils.hasText(item.getItemName())) {
                                                /*Item item <- 이 이름*/
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000원에서 100만원까지 허용않습니다"));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }


        // 특정 필드가 아닌 복합 룰 검증
        // new ObjectError!
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            /* bindResult는 view 로 알아서 가지고 간다.*/
            return "validation/v2/addForm";
        }

        // 아래는 성공 로직!
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes
    ) {

        //검증 로직
        // 필드 오류!!! Item 필드에 대한 오류 new FieldError
        if (!StringUtils.hasText(item.getItemName())) {
                                                                                        /*rejectedValue,        바인딩이 잘되긴햇는가?    ??           ??  */
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false,null,null,"상품 이름은 필수입니다."));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false,null,null,"가격은 1,000원에서 100만원까지 허용않습니다"));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false,null,null,"수량은 최대 9,999 까지 허용합니다."));
        }


        // 특정 필드가 아닌 복합 룰 검증
        // new ObjectError!
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item",null, null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            /* bindResult는 view 로 알아서 가지고 간다.*/
            return "validation/v2/addForm";
        }

        // 아래는 성공 로직!
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes
    ) {
        log.info("object name={}", bindingResult.getObjectName()); // object name=item
        log.info("target={}", bindingResult.getTarget()); // target=Item(id=null, itemName=, price=null, quantity=null)

        //검증 로직
        // 필드 오류!!! Item 필드에 대한 오류 new FieldError
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item",
                    "itemName", item.getItemName(),
                    false,
                    new String[]{"required.item.itemName"}, //code
                    null,
                    null));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item",
                    "price", item.getPrice(),
                    false,
                    new String[]{"range.item.price"},
                    new Object[]{1000, 1000000},
                    null));
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item",
                    "quantity",
                    item.getQuantity(),
                    false,
                    new String[]{"max.item.quantity"},
                    new Object[]{9999},
                    null));
        }

        // 특정 필드가 아닌 복합 룰 검증
        // new ObjectError!
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item",
                        new String[]{"totalPriceMin"},
                        new Object[]{10000, resultPrice},
                        null));
            }
        }

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            /* bindResult는 view 로 알아서 가지고 간다.*/
            return "validation/v2/addForm";
        }

        // 아래는 성공 로직!
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes
    ) {
        log.info("object name={}", bindingResult.getObjectName()); // object name=item
        log.info("target={}", bindingResult.getTarget()); // target=Item(id=null, itemName=, price=null, quantity=null)



        //검증 로직
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName", "required");
        }
          /* ↕↕↕↕↕↕↕↕↕ 위 아래는 같은 것이다. */
//        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "require");


        /*
            if 조건을 조절해서
                typeMismatch 메세지와 range와 따로 따로 보여줄 수 있따.
        */
        if (item.getPrice() != null && (item.getPrice() < 1000 || item.getPrice() > 1000000)) {
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }

        // 특정 필드가 아닌 복합 룰 검증
        // new ObjectError!
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
            return "validation/v2/addForm";
        }

        // 아래는 성공 로직!
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    // Validator분리하기
//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes
    ) {

        if (itemValidator.supports(item.getClass())) {
            itemValidator.validate(item, bindingResult);
        }


        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            /* bindResult는 view 로 알아서 가지고 간다.*/
            return "validation/v2/addForm";
        }

        // 아래는 성공 로직!
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes
    ) {

        //검증에 실패하면 다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            /* bindResult는 view 로 알아서 가지고 간다.*/
            return "validation/v2/addForm";
        }

        // 아래는 성공 로직!
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }




    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

