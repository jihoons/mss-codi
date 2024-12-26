import {client} from "./codiApi.ts";
import {toast} from "react-toastify";

export interface Brand {
    id: number;
    name: string;
}

export const getBrands = async () => {
    const response = await client.get("/brand");
    if (response) {
        return response.data as Brand[];
    } else {
        return [];
    }
}

export const addBrand = async (name: string) => {
    const response = await client.post("/brand", {
        name
    });
    if (response) {
        toast.success("저장되었습니다.");
        return response.data as Brand;
    } else {
        return null;
    }
}

export interface Product {
    id: number;
    category: string;
    brand: string;
    price: number;
}

export const getProducts = async (category: string, brand: string) => {
    const response = await client.get(`/product?${category ? `category=${encodeURIComponent(category)}&` : ""}${brand ? `brand=${encodeURIComponent(brand)}`: ""}`)
    if (response) {
        return response.data as Product[];
    } else {
        return [];
    }
}

export const addProduct = async (product: Product) => {
    const response = await client.post(`/product`, product);
    if (response) {
        return response.data as Product;
    } else {
        return null;
    }
}

export const modifyProduct = async (product: Product) => {
    const response = await client.put(`/product`, product);
    if (response) {
        return response.data as Product;
    } else {
        return null;
    }
}

export const deleteProduct = async (productId: number) => {
    const response = await client.delete(`/product/${productId}`);
    return !!response;
}
