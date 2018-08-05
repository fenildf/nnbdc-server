package beidanci.vo;


import java.util.List;

public class DictGroupVo extends Vo {

    private String name;
    private List<DictVo> dicts;
    private DictGroupVo dictGroup;

    public List<DictVo> getAllDicts() {
        return allDicts;
    }

    public void setAllDicts(List<DictVo> allDicts) {
        this.allDicts = allDicts;
    }

    private List<DictVo> allDicts;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DictVo> getDicts() {
        return dicts;
    }

    public void setDicts(List<DictVo> dicts) {
        this.dicts = dicts;
    }

    public DictGroupVo getDictGroup() {
        return dictGroup;
    }

    public void setDictGroup(DictGroupVo dictGroup) {
        this.dictGroup = dictGroup;
    }
}
