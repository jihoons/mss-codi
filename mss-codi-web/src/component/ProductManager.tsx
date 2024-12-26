import {
    Box,
    Button, Card, CardActions, CardContent,
    MenuItem, Modal,
    Select,
    Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow, TextField,
    Typography
} from "@mui/material";
import {useEffect, useState} from "react";
import {addProduct, Brand, deleteProduct, getBrands, getProducts, modifyProduct, Product} from "../api/productApi.ts";
import {Category, getAllCategories} from "../api/categoryApi.ts";
import {toast} from "react-toastify";

const ProductManager = () => {
    const [brands, setBrands] = useState<Brand[]>([])
    const [categories, setCategories] = useState<Category[]>([]);
    const [selectedBrand, setSelectedBrand] = useState("-")
    const [selectedCategory, setSelectedCategory] = useState("-")
    const [products, setProducts] = useState<Product[]>([])

    const [showEditModal, setShowEditModal]= useState(false);
    const [editProduct, setEditProduct] = useState<Product>({
        id: 0,
        category: "-",
        brand: "-",
        price: 0
    })

    const getBrand = async() => {
        const response = await getBrands()
        setBrands(response)
    }

    const getCategories = async () => {
        const allCategories = await getAllCategories() ?? [];
        setCategories(allCategories);
    }

    const onChangeBrand = (e:any) => {
        setSelectedBrand(e.target.value);
    }

    const onChangeCategory = (e: any) => {
        setSelectedCategory(e.target.value);
    }


    const onClickGetProducts = async () => {
        const response = await getProducts(selectedCategory === "-" ? "" : selectedCategory, selectedBrand === "-" ? "" : selectedBrand);
        setProducts(response)
    }

    const getCategoryItems = () => {
        const defaultItem = <MenuItem key={"-"} value={"-"}>카테고리 선택</MenuItem>
        return [defaultItem, ...categories.map(category => <MenuItem key={category.name} value={category.name}>{category.name}</MenuItem>)];
    }

    const getBrandItems = () => {
        const defaultItem = <MenuItem key={"-"} value={"-"}>브랜드 선택</MenuItem>
        return [defaultItem, ...brands.map(brand => <MenuItem key={brand.name} value={brand.name}>{brand.name}</MenuItem>)];
    }

    const onClickAddProduct = () => {
        setEditProduct({
                id: 0,
                category: selectedCategory ?? "-",
                brand: selectedBrand ?? "-",
                price: 0
        });
        setShowEditModal(true)
    }

    const onClickRemoveProduct = async (product: Product) => {
        const success = await deleteProduct(product.id)
        if (success) {
            setProducts(products.filter(p => p.id !== product.id));
            toast.success(`삭제되었습니다.`);
        }
    }

    const [canSave, setCanSave] = useState(false);
    const onClickModifyProduct = (product: Product) => {
        setShowEditModal(true);
        setEditProduct(product);
    }

    const onCloseModal = () => {
        setShowEditModal(false);
    }

    const modifyProductOnModal = async () => {
        // 수정
        const modifiedProduct = await modifyProduct(editProduct)
        if (modifiedProduct) {
            const newProducts = products.map(p => {
                if (p.id == modifiedProduct.id) {
                    return modifiedProduct;
                } else {
                    return p;
                }
            });
            setProducts(newProducts);
        }

        return modifiedProduct;
    }

    const addProductOnModal = async () => {
        // 추가
        const addedProduct = await addProduct(editProduct)
        if (addedProduct && selectedCategory === addedProduct.category && selectedBrand === addedProduct.brand) {
            setProducts([...products, addedProduct]);
        }

        return addedProduct;
    }

    const onClickSaveProduct = async () => {
        let product: Product | null = null;
        if (editProduct.id > 0) {
            product = await modifyProductOnModal();
        } else {
            product = await addProductOnModal();
        }

        if (product) {
            setShowEditModal(false);
        }
    }

    useEffect(() => {
        setCanSave(editProduct.category !== "-" && editProduct.brand !== "-" && editProduct.price > 0)
    }, [editProduct]);

    const onChangedEditModalCategory = (e: any) => {
        setEditProduct({
            ...editProduct,
            category: e.target.value,
        })
    }

    const onChangedEditModalBrand = (e: any) => {
        setEditProduct({
            ...editProduct,
            brand: e.target.value,
        })
    }

    const onChangedEditModalPrice = (e: any) => {
        const price = Number(e.target.value);
        if (isNaN(price)) {
            return;
        }
        setEditProduct({
            ...editProduct,
            price,
        })
    }

    const style = {
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: 400,
    };

    useEffect(() => {
        getBrand();
        getCategories();
    }, []);

    return (
        <>
            <Stack gap={"16px"}>
                <Typography>상품 관리</Typography>
                <Box gap={"20px"} display={"flex"} flexDirection={"row"} >
                    <Box flexDirection={"column"}>
                        <div>카테고리</div>
                        <Select size={"small"} variant={"outlined"} value={selectedCategory} onChange={onChangeCategory} sx={{width: "200px"}}>
                            {getCategoryItems()}
                        </Select>
                    </Box>
                    <Box flexDirection={"column"}>
                        <div>브랜드</div>
                        <Select size={"small"} variant={"outlined"} value={selectedBrand} onChange={onChangeBrand} sx={{width: "200px"}}>
                            {getBrandItems()}
                        </Select>
                    </Box>
                    <Button variant={"contained"} sx={{height: "40px", marginTop: "20px"}} onClick={onClickGetProducts}>조회</Button>
                    <Button variant={"contained"} sx={{height: "40px", marginTop: "20px"}} onClick={onClickAddProduct}>추가</Button>
                </Box>
                <Table>
                    <TableHead>
                        <TableRow sx={{background: "#f0f0f0"}}>
                            <TableCell>카테고리</TableCell>
                            <TableCell>브랜드</TableCell>
                            <TableCell>가격</TableCell>
                            <TableCell></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {products.map(product => {
                            return <TableRow key={product.id}
                                             sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                <TableCell>{product.category}</TableCell>
                                <TableCell>{product.brand}</TableCell>
                                <TableCell>{product.price}</TableCell>
                                <TableCell>
                                    <Box>
                                        <Button onClick={() => onClickModifyProduct(product)}>수정</Button>
                                        <Button onClick={() => onClickRemoveProduct(product)}>삭제</Button>
                                    </Box>
                                </TableCell>
                            </TableRow>
                        })}
                    </TableBody>
                </Table>
            </Stack>
            <Modal open={showEditModal} onClose={onCloseModal}>
                <Box sx={style}>
                    <Card variant={"outlined"}>
                        <CardContent>
                            <Stack gap={"16px"}>
                                <Box flexDirection={"column"}>
                                    <div>카테고리</div>
                                    <Select variant={"outlined"} size={"small"} value={editProduct.category} onChange={onChangedEditModalCategory} sx={{width: "200px"}}>
                                        {getCategoryItems()}
                                    </Select>
                                </Box>
                                <Box flexDirection={"column"}>
                                    <div>브랜드</div>
                                    <Select variant={"outlined"} size={"small"} value={editProduct.brand} onChange={onChangedEditModalBrand} sx={{width: "200px"}}>
                                        {getBrandItems()}
                                    </Select>
                                </Box>
                                <Box flexDirection={"column"}>
                                    <div>가격</div>
                                    <TextField type={"number"} size={"small"} value={editProduct.price} onChange={onChangedEditModalPrice} sx={{width: "200px"}}>
                                    </TextField>
                                </Box>
                            </Stack>
                        </CardContent>
                        <CardActions>
                            <Box display={"flex"} justifyContent={"end"} sx={{width: "100%"}}>
                                <Button onClick={() => setShowEditModal(false)}>취소</Button>
                                <Button variant={"contained"} onClick={onClickSaveProduct} disabled={!canSave}>저장</Button>
                            </Box>
                        </CardActions>
                    </Card>
                </Box>
            </Modal>
        </>
    );
}

export default ProductManager;