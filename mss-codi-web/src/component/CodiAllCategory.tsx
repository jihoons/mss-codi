import {useEffect, useState} from "react";
import {BrandOfCategory, getCodiAllCategory, PriceType} from "../api/codiApi.ts";
import {MenuItem, Select, Stack, Table, TableBody, TableCell, TableHead, TableRow} from "@mui/material";

const CodiAllCategory = () => {
    const [priceType, setPriceType] = useState<PriceType>("min");
    const [categories, setCategories] = useState<BrandOfCategory[]>([])
    const [totalPrice, setTotalPrice] = useState<string>("");

    const getData = async () => {
        const response = await getCodiAllCategory(priceType)
        if (response) {
            setCategories(response["카테고리별상품"] ?? []);
            setTotalPrice(response["총액"] ?? "");
        }
    }

    const onChangePriceType = (e: any) => {
        setPriceType(e.target.value);
    }

    useEffect(() => {
        getData();
    }, [priceType]);

    return (
        <Stack gap={"16px"}>
            <Select value={priceType} label="최저가/최고가 선택" variant={"filled"} size={"small"} onChange={onChangePriceType}>
                <MenuItem value={"min"}>최저가</MenuItem>
                <MenuItem value={"max"}>최고가</MenuItem>
            </Select>
            <Table>
                <TableHead>
                    <TableRow sx={{background: "#f0f0f0"}}>
                        <TableCell>카테고리</TableCell>
                        <TableCell>브랜드</TableCell>
                        <TableCell>가격</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {categories.map(category => {
                        return <TableRow key={category["카테고리"]}
                                         sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                            <TableCell>{category["카테고리"]}</TableCell>
                            <TableCell>{category["브랜드"]}</TableCell>
                            <TableCell>{category["가격"]}</TableCell>
                        </TableRow>
                    })}
                    {totalPrice &&
                        <TableRow sx={{background: "#f0f0f0"}}>
                            <TableCell colSpan={2} align={"center"}>총액</TableCell>
                            <TableCell>{totalPrice}</TableCell>
                        </TableRow>
                    }
                </TableBody>
            </Table>
        </Stack>
    );
}

export default CodiAllCategory;