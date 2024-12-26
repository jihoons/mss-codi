import {useEffect, useState} from "react";
import {Category, getAllCategories} from "../api/categoryApi.ts";
import {
    Card,
    CardContent,
    MenuItem,
    Select,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Typography
} from "@mui/material";
import {CategoryBrand, getMinMaxProductByCategory, MinMaxProductByCategoryResponse} from "../api/codiApi.ts";

const CodiOneCategory = () => {
    const [categories, setCategories] = useState<Category[]>([]);
    const [selectedCategory, setSelectedCategory] = useState<number>(-1)
    const [minMaxCategory, setMinMaxCategory] = useState<MinMaxProductByCategoryResponse | null>(null)

    const getCategories = async () => {
        const allCategories = await getAllCategories() ?? [];
        setCategories(allCategories);
    }

    const getData = async () => {
        if (categories && selectedCategory > 0) {
            const categoryName = categories.find(c => c.id === selectedCategory)?.name;
            if (categoryName) {
                const response = await getMinMaxProductByCategory(categoryName)
                setMinMaxCategory(response)
            } else {
                setMinMaxCategory(null);
            }
        } else {
            setMinMaxCategory(null);
        }
    }

    const onChangeCategory = (e: any) => {
        setSelectedCategory(e.target.value)
    }

    useEffect(() => {
        getCategories()
    }, []);

    useEffect(() => {
        getData();
    }, [selectedCategory]);

    const getCategoryItems = () => {
        const defaultItem = <MenuItem key={-1} value={-1}>{"카테고리 선택"}</MenuItem>;
        const items = categories.map(c => {
            return <MenuItem key={c.id} value={c.id}>{c.name}</MenuItem>
        });
        return [defaultItem, ...items];
    }

    const getTable = (categoryBrands: CategoryBrand[]) => {
        return (
            <Table>
                <TableHead>
                    <TableRow sx={{background: "#f0f0f0"}}>
                        <TableCell>브랜드</TableCell>
                        <TableCell>가격</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {categoryBrands.map(brand => {
                        return <TableRow key={brand["브랜드"]}
                                         sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                            <TableCell>{brand["브랜드"]}</TableCell>
                            <TableCell>{brand["가격"]}</TableCell>
                        </TableRow>
                    })}
                </TableBody>
            </Table>
        );
    }

    return (
        <Stack gap={"16px"}>
            {categories &&
                <Select value={selectedCategory} label="카테고리 선택" variant={"filled"} size={"small"} onChange={onChangeCategory}>
                    {getCategoryItems()}
                </Select>
            }
            {minMaxCategory &&
                <>
                    <Typography>{`검색 카테고리: ${minMaxCategory["카테고리"]}`}</Typography>
                    <Card>
                        <CardContent>
                            <Typography variant={"h6"}>최저가 브랜드 {`${minMaxCategory["최저가"].length} 개`}</Typography>
                            {getTable(minMaxCategory["최저가"])}
                        </CardContent>
                    </Card>
                    <Card>
                        <CardContent>
                            <Typography variant={"h6"}>최고가 브랜드 {`${minMaxCategory["최고가"].length} 개`}</Typography>
                            {getTable(minMaxCategory["최고가"])}
                        </CardContent>
                    </Card>
                </>
            }
        </Stack>
    );
}

export default CodiOneCategory;