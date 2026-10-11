package phoenix.AM_PM.global.pagination;

import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class PageResponseDto<T> {
    private List<T> data;
    private PageInfo pageInfo;
    private List<Integer> barNumber;

    public PageResponseDto(List<T> data, Slice slice){
        this.data = data;
        this.pageInfo = new PageInfo(slice.getNumber() + 1,
                slice.getSize(), slice.hasNext());
    }
}
