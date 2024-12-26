import {Button, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography} from "@mui/material";
import {useEffect, useState} from "react";
import {addBrand, Brand, getBrands} from "../api/productApi.ts";

const BrandManager = () => {
    const [brands, setBrands] = useState<Brand[]>([])
    const [name, setName] = useState("")

    const loadBrand = async() => {
        const response = await getBrands()
        setBrands(response)
    }

    const saveBrand = async () => {
        const newBrand = await addBrand(name)
        if (newBrand) {
            setBrands([...brands, newBrand]);
        }
    }

    const onClickAddBrand = () => {
        saveBrand()
    }

    const onChangeName = (e: any) => {
        setName(e.target.value)
    }

    useEffect(() => {
        loadBrand();
    }, []);

    return (
        <Stack gap={"16px"}>
            <Typography>브랜드 추가</Typography>
            <TextField value={name} onChange={onChangeName} />
            <Button onClick={onClickAddBrand} variant={"contained"} disabled={!name}>추가</Button>
            {brands.length > 0 &&
                <Table>
                    <TableHead>
                        <TableRow sx={{background: "#f0f0f0"}}>
                            <TableCell>브랜드 목록</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {brands.map(brand => {
                            return <TableRow key={brand.name}
                                             sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                <TableCell>{brand.name}</TableCell>
                            </TableRow>
                            })
                        }
                    </TableBody>
                </Table>
            }
        </Stack>
    );
}

export default BrandManager;