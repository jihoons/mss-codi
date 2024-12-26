import {ListItemText, MenuItem, MenuList, styled} from "@mui/material";
import {useNavigate} from "react-router-dom";

const SideMenuList = styled('div')({
    display: "flex",
    flexDirection: "column",
    gap: "8px",
    width: "220px",
    height: "100vh",
})

const SideMenu = () => {
    const navigate = useNavigate();
    const onMoveMenu = (link: string) => {
        navigate(link);
    }

    return (
        <SideMenuList>
            <MenuList>
                <MenuItem>
                    <ListItemText onClick={() => onMoveMenu("/all")}>카테고리별 가격 코디</ListItemText>
                </MenuItem>
                <MenuItem>
                    <ListItemText onClick={() => onMoveMenu("/brand")}>단일브랜드 코디</ListItemText>
                </MenuItem>
                <MenuItem>
                    <ListItemText onClick={() => onMoveMenu("/category")}>카테고리 브랜드 조회</ListItemText>
                </MenuItem>
                <MenuItem>
                    <ListItemText onClick={() => onMoveMenu("/product/brand")}>브랜드 관리</ListItemText>
                </MenuItem>
                <MenuItem>
                    <ListItemText onClick={() => onMoveMenu("/product/product")}>상품 관리</ListItemText>
                </MenuItem>
            </MenuList>
        </SideMenuList>
    );
}

export default SideMenu;