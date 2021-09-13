package vn.com.hust.son.stockuserid.conteoller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heathAbc")
public class Controller {
    @GetMapping
    public String heath(){
        return "heath done";
    }
}
