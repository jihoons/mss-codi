import {MenuItem, Select, Stack, Table, TableBody, TableCell, TableHead, TableRow, Typography} from "@mui/material";
import {useEffect, useState} from "react";
import {Brand, getCodeOneBrand, PriceType} from "../api/codiApi.ts";

const CodiOneBrand = () => {
    const [priceType, setPriceType] = useState<PriceType>("min");
    const [brand, setBrand] = useState<Brand | null>(null);

    const onChangePriceType = (e: any) => {
        setPriceType(e.target.value);
    }

    const getData = async() => {
        const response = await getCodeOneBrand(priceType);
        if (response) {
            setBrand(response[priceType == "min" ? "최저가" : "최고가"]);
        }
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
            {brand &&
                <>
                    <Typography>{`${priceType === "min" ? "최저가" : "최고가" } 브랜드`} {brand["브랜드"]}</Typography>
                    <Table>
                        <TableHead>
                            <TableRow sx={{background: "#f0f0f0"}}>
                                <TableCell>카테고리</TableCell>
                                <TableCell>가격</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {brand["카테고리"].map(category => {
                                return <TableRow key={category["카테고리"]}
                                                 sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                    <TableCell>{category["카테고리"]}</TableCell>
                                    <TableCell>{category["가격"]}</TableCell>
                                </TableRow>
                            })}
                            <TableRow sx={{background: "#f0f0f0"}}>
                                <TableCell>총액</TableCell>
                                <TableCell>{brand["총액"]}</TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </>
            }
        </Stack>
    );
}

export default CodiOneBrand;