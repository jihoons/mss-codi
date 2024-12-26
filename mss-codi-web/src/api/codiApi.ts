import axios from "axios";
import {toast} from "react-toastify";

export const client = axios.create({
    baseURL: import.meta.env.VITE_API_URL
});

export type PriceType = "min" | "max";

export interface CodiAllCategoryResponse {
    "카테고리별상품": BrandOfCategory[];
    "총액": string;
}

export interface BrandOfCategory {
    "카테고리": string;
    "브랜드": string;
    "가격": string;
}

export const getCodiAllCategory = async (priceType: PriceType = "min") => {
    const response = await client.get(`/codi/all?priceType=${priceType}`)
    if (response.status == 200) {
        return response.data as CodiAllCategoryResponse;
    } else {
        toast.error(response.data?.message ?? "오류가 발생했습니다.");
        return null;
    }
}

export interface CodiTargetPriceBrandResponse {
    "최저가": Brand;
    "최고가": Brand;
}

export interface Brand {
    "브랜드": string;
    "카테고리": PriceOfCategory[];
    "총액": string;
}

interface PriceOfCategory {
    "카테고리": string;
    "가격": string;
}

export const getCodeOneBrand = async (priceType: PriceType = "min") => {
    const response = await client.get(`/codi/brand?priceType=${priceType}`)
    if (response.status == 200) {
        return response.data as CodiTargetPriceBrandResponse;
    } else {
        toast.error(response.data?.message ?? "오류가 발생했습니다.");
        return null;
    }
}

export interface MinMaxProductByCategoryResponse {
    "카테고리": string;
    "최저가": CategoryBrand[];
    "최고가": CategoryBrand[];
}

export interface CategoryBrand {
    "브랜드": string;
    "가격": string;
}

export const getMinMaxProductByCategory = async (category: string) => {
    const response = await client.get(`/codi/category?category=${encodeURIComponent(category)}`)
    if (response.status == 200) {
        return response.data as MinMaxProductByCategoryResponse;
    } else {
        toast.error(response.data?.message ?? "오류가 발생했습니다.");
        return null;
    }
}