package io.github.stock.sdk.service;

import io.github.stock.sdk.model.Plate;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.provider.DataProvider;

import java.util.List;

/**
 * 行业与概念板块命名空间服务 (sdk.plate.* / sdk.board.*)
 */
public class PlateService {

    private final DataProvider provider;

    public PlateService(DataProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取全部行业板块列表及涨跌行情
     */
    public List<Plate> industry() {
        return provider.getPlates(2);
    }

    /**
     * 获取全部概念板块列表及涨跌行情
     */
    public List<Plate> concept() {
        return provider.getPlates(3);
    }

    /**
     * 获取指定板块的成分股列表行情
     * @param plateCode 板块代码，如 BK0420 (贵金属)
     */
    public List<Quote> members(String plateCode) {
        return provider.getPlateMembers(plateCode);
    }
}
