import {client} from "./codiApi.ts";

export interface Category {
    id: number;
    name: string;
    displayOrder: number;
}

export const getAllCategories = async () => {
    const response = await client.get("/category");
    if (response) {
        return response.data as Category[];
    } else {
        return [];
    }
}