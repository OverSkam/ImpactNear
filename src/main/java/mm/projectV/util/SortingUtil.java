package mm.projectV.util;

import org.springframework.data.domain.Sort;

public class SortingUtil {
    public static Sort sortGenerator(String sortBy, String sortDirection){
        Sort sort = Sort.by(Sort.Order.by(sortBy));
        if (sortDirection.equalsIgnoreCase("desc"))
            return sort.descending();
        else
            return sort.ascending();
    }
}
